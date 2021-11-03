package myapp


import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KProperty1

abstract class ReduxViewModel<S>(
    initialState: S
) : ViewModel(), Observable {
    private val state = MutableStateFlow(initialState)
    private val stateMutex = Mutex()

    /**
     * Returns a snapshot of the current state.
     */
    fun currentState(): S = state.value

    val liveData: LiveData<S>
        get() = state.asLiveData()

    protected suspend fun <T> Flow<T>.collectAndSetState(reducer: S.(T) -> S) {
        return collect { item -> setState { reducer(item) } }
    }

    fun <A> liveFieldOf(prop1: KProperty1<S, A>): LiveData<A> {
        return flowFieldOf(prop1).asLiveData()
    }

    fun <A> liveStateOf(prop1: KProperty1<S, A>): LiveData<S> {
        return flowStateOf(prop1).asLiveData()
    }

    protected fun subscribe(block: (S) -> Unit) {
        viewModelScope.launch {
            state.collect { block(it) }
        }
    }

    protected fun <A> subscribeStateOf(prop1: KProperty1<S, A>, block: (S) -> Unit) {
        viewModelScope.launch {
            flowFieldOf(prop1).collect { block(currentState()) }
        }
    }

    protected fun <A> subscribeField(prop1: KProperty1<S, A>, block: (A) -> Unit) {
        viewModelScope.launch {
            flowFieldOf(prop1).collect { block(it) }
        }
    }

    fun flowFieldsOf(props: Collection<KProperty1<S, *>>): Flow<Long> {
        return combine(props.map { prop ->
            state.map { prop.get(it) }.distinctUntilChanged()
        }) {
            System.currentTimeMillis()
        }
    }

    fun <A> flowFieldOf(prop1: KProperty1<S, A>): Flow<A> {
        return state.map { prop1.get(it) }.distinctUntilChanged()
    }

    fun <A> flowStateOf(prop1: KProperty1<S, A>): Flow<S> {
        return flowFieldOf(prop1).map { state.value }
    }

    protected suspend fun setState(reducer: S.() -> S) {
        stateMutex.withLock {
            state.value = reducer(state.value)
        }
    }

    protected fun CoroutineScope.launchSetState(reducer: S.() -> S) {
        launch { this@ReduxViewModel.setState(reducer) }
    }

    protected suspend fun withState(block: (S) -> Unit) {
        stateMutex.withLock {
            block(state.value)
        }
    }

    protected fun CoroutineScope.withState(block: (S) -> Unit) {
        launch { this@ReduxViewModel.withState(block) }
    }


    /////////// for data binding ////

    // copy of BaseObservable
    @Transient
    private var mCallbacks: PropertyChangeRegistry? = null

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                mCallbacks = PropertyChangeRegistry()
            }
        }
        mCallbacks!!.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks!!.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange() {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks!!.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks!!.notifyCallbacks(this, fieldId, null)
    }
}
