package myapp.extensions

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*


fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this

// https://gist.github.com/magneticflux-/044c9d7a3cea431aa0e4f4f4950a2898
fun <A, B, RESULT> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    combineFunction: (A, B) -> RESULT
): MutableLiveData<RESULT> {
    return MediatorLiveData<RESULT>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val aa = lastA ?: return
            val bb = lastB ?: return
            this.value = combineFunction(aa, bb)
        }

        addSource(a) { lastA = it; update() }
        addSource(b) { lastB = it; update() }
    }
}


fun <A, B, C, RESULT> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    c: LiveData<C>,
    combineFunction: (A, B, C) -> RESULT
): MutableLiveData<RESULT> {
    return MediatorLiveData<RESULT>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null

        fun update() {
            val aa = lastA ?: return
            val bb = lastB ?: return
            val cc = lastC ?: return
            this.value = combineFunction(aa, bb, cc)
        }

        addSource(a) { lastA = it; update() }
        addSource(b) { lastB = it; update() }
        addSource(c) { lastC = it; update() }
    }
}


fun <A, B, C, D, RESULT> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    c: LiveData<C>,
    d: LiveData<D>,
    combineFunction: (A, B, C, D) -> RESULT
): MutableLiveData<RESULT> {
    return MediatorLiveData<RESULT>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null

        fun update() {
            val aa = lastA ?: return
            val bb = lastB ?: return
            val cc = lastC ?: return
            val dd = lastD ?: return
            this.value = combineFunction(aa, bb, cc, dd)
        }

        addSource(a) { lastA = it; update() }
        addSource(b) { lastB = it; update() }
        addSource(c) { lastC = it; update() }
        addSource(d) { lastD = it; update() }
    }
}


fun <A, B, C, D, E, RESULT> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    c: LiveData<C>,
    d: LiveData<D>,
    e: LiveData<E>,
    combineFunction: (A, B, C, D, E) -> RESULT
): MutableLiveData<RESULT> {
    return MediatorLiveData<RESULT>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null
        var lastE: E? = null

        fun update() {
            val aa = lastA ?: return
            val bb = lastB ?: return
            val cc = lastC ?: return
            val dd = lastD ?: return
            val ee = lastE ?: return
            this.value = combineFunction(aa, bb, cc, dd, ee)
        }

        addSource(a) { lastA = it; update() }
        addSource(b) { lastB = it; update() }
        addSource(c) { lastC = it; update() }
        addSource(d) { lastD = it; update() }
        addSource(e) { lastE = it; update() }
    }
}


fun <A, B, C, D, E, F, RESULT> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    c: LiveData<C>,
    d: LiveData<D>,
    e: LiveData<E>,
    f: LiveData<F>,
    combineFunction: (A, B, C, D, E, F) -> RESULT
): MutableLiveData<RESULT> {
    return MediatorLiveData<RESULT>().apply {
        var lastA: A? = null
        var lastB: B? = null
        var lastC: C? = null
        var lastD: D? = null
        var lastE: E? = null
        var lastF: F? = null

        fun update() {
            val aa = lastA ?: return
            val bb = lastB ?: return
            val cc = lastC ?: return
            val dd = lastD ?: return
            val ee = lastE ?: return
            val ff = lastF ?: return
            this.value = combineFunction(aa, bb, cc, dd, ee, ff)
        }

        addSource(a) { lastA = it; update() }
        addSource(b) { lastB = it; update() }
        addSource(c) { lastC = it; update() }
        addSource(d) { lastD = it; update() }
        addSource(e) { lastE = it; update() }
        addSource(f) { lastF = it; update() }
    }
}


// https://github.com/adibfara/Lives/blob/master/lives/src/main/java/com/snakydesign/livedataextensions/Filtering.kt
class SingleLiveData<T>(liveData: LiveData<T>) : MediatorLiveData<T>() {
    private var hasSetValue = false
    private val mediatorObserver = Observer<T> {
        if (!hasSetValue) {
            hasSetValue = true
            this@SingleLiveData.value = it
        }
    }

    init {
        if (liveData.value != null) {
            hasSetValue = true
            this.value = liveData.value
        } else {
            addSource(liveData, mediatorObserver)
        }
    }
}

/**
 * Emits at most 1 item and returns a SingleLiveData
 */
fun <T> LiveData<T>.first(): SingleLiveData<T> {
    return SingleLiveData(this)
}


/**
 * Emits the first n valueus
 */
fun <T> LiveData<T>.take(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var takenCount = 0
    mutableLiveData.addSource(this) {
        if (takenCount < count) {
            mutableLiveData.value = it
            takenCount++
        } else {
            mutableLiveData.removeSource(this)
        }
    }
    return mutableLiveData
}

/**
 * Takes until a certain predicate is met, and does not emit anything after that, whatever the value.
 */
inline fun <T> LiveData<T>.takeUntil(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var metPredicate = predicate(value)
    mutableLiveData.addSource(this) {
        if (predicate(it)) metPredicate = true
        if (!metPredicate) {
            mutableLiveData.value = it
        } else {
            mutableLiveData.removeSource(this)
        }
    }
    return mutableLiveData
}

/**
 * Skips the first n values
 */
fun <T> LiveData<T>.skip(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var skippedCount = 0
    mutableLiveData.addSource(this) {
        if (skippedCount >= count) {
            mutableLiveData.value = it
        }
        skippedCount++
    }
    return mutableLiveData
}

/**
 * Skips all values until a certain predicate is met (the item that actives the predicate is also emitted)
 */
inline fun <T> LiveData<T>.skipUntil(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var metPredicate = false
    mutableLiveData.addSource(this) {
        if (metPredicate || predicate(it)) {
            metPredicate = true
            mutableLiveData.value = it
        }
    }
    return mutableLiveData
}


/**
 * Emits the items that pass through the predicate
 */
inline fun <T> LiveData<T>.filter(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) {
        if (predicate(it))
            mutableLiveData.value = it
    }
    return mutableLiveData
}


/**
 * Mapper function used in the operators that need mapping
 */
typealias OnNextAction<T> = (T?) -> Unit

/**
 * Does the `onNext` function before everything actually emitting the item to the observers
 */
fun <T> LiveData<T>.doBeforeNext(onNext: OnNextAction<T>): MutableLiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) {
        onNext(it)
        mutableLiveData.value = it
    }
    return mutableLiveData
}

/**
 * Does the `onNext` function after emitting the item to the observers
 */
fun <T> LiveData<T>.doAfterNext(onNext: OnNextAction<T>): MutableLiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) {
        mutableLiveData.value = it
        onNext(it)
    }
    return mutableLiveData
}


// https://gist.github.com/lupajz/43068881d949e207d5efe2c40bde1866
fun <T> LiveData<T>.debounce(duration: Long = 1000L) = MediatorLiveData<T>().also { mld ->
    val source = this
    val handler = Handler(Looper.getMainLooper())

    val runnable = Runnable {
        mld.value = source.value
    }

    mld.addSource(source) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, duration)
    }
}


class NonNullMediatorLiveData<T> : MediatorLiveData<T>()

fun <T> LiveData<T?>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this, Observer { it?.let { mediator.value = it } })
    return mediator
}


fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}


inline fun <T> LiveData<T>.observeK(owner: LifecycleOwner, crossinline observer: (T?) -> Unit) {
    this.observe(owner, Observer { observer(it) })
}

inline fun <T> LiveData<T>.observeNotNull(
    owner: LifecycleOwner,
    crossinline observer: (T) -> Unit
) {
    this.observe(owner, Observer { it?.run(observer) })
}

