@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package myapp.util

typealias Action0 = () -> Unit
typealias Action1<T> = (T) -> Unit
typealias Action2<T1, T2> = (T1, T2) -> Unit
typealias Action3<T1, T2, T3> = (T1, T2, T3) -> Unit
typealias Action4<T1, T2, T3, T4> = (T1, T2, T3, T4) -> Unit

typealias Func0<R> = () -> R
typealias Func1<T, R> = (T) -> R
typealias Func2<T1, T2, R> = (T1, T2) -> R
typealias Func3<T1, T2, T3, R> = (T1, T2, T3) -> R

data class Tuple2<T1, T2>(val data1: T1, val data2: T2)
data class Tuple3<T1, T2, T3>(val data1: T1, val data2: T2, val data3: T3)
data class Tuple4<T1, T2, T3, T4>(val data1: T1, val data2: T2, val data3: T3, val data4: T4)
data class Tuple5<T1, T2, T3, T4, T5>(val data1: T1, val data2: T2, val data3: T3, val data4: T4, val data5: T5)
data class Tuple6<T1, T2, T3, T4, T5, T6>(
    val data1: T1,
    val data2: T2,
    val data3: T3,
    val data4: T4,
    val data5: T5,
    val data6: T6
)

data class Tuple7<T1, T2, T3, T4, T5, T6, T7>(
    val data1: T1,
    val data2: T2,
    val data3: T3,
    val data4: T4,
    val data5: T5,
    val data6: T6,
    val data7: T7,
)

data class Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(
    val data1: T1,
    val data2: T2,
    val data3: T3,
    val data4: T4,
    val data5: T5,
    val data6: T6,
    val data7: T7,
    val data8: T8,
)

data class Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    val data1: T1,
    val data2: T2,
    val data3: T3,
    val data4: T4,
    val data5: T5,
    val data6: T6,
    val data7: T7,
    val data8: T8,
    val data9: T9,
)

fun <T1, T2> tupleOf(data1: T1, data2: T2) = Tuple2(data1, data2)
fun <T1, T2, T3> tupleOf(data1: T1, data2: T2, data3: T3) = Tuple3(data1, data2, data3)
fun <T1, T2, T3, T4> tupleOf(data1: T1, data2: T2, data3: T3, data4: T4) = Tuple4(data1, data2, data3, data4)
fun <T1, T2, T3, T4, T5> tupleOf(data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) =
    Tuple5(data1, data2, data3, data4, data5)

fun <T1, T2, T3, T4, T5, T6> tupleOf(data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6) =
    Tuple6(data1, data2, data3, data4, data5, data6)

fun <T1, T2, T3, T4, T5, T6, T7> tupleOf(data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6, data7: T7) =
    Tuple7(data1, data2, data3, data4, data5, data6, data7)

fun <T1, T2, T3, T4, T5, T6, T7, T8> tupleOf(
    data1: T1,
    data2: T2,
    data3: T3,
    data4: T4,
    data5: T5,
    data6: T6,
    data7: T7,
    data8: T8
) = Tuple8(data1, data2, data3, data4, data5, data6, data7, data8)


fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> tupleOf(
    data1: T1,
    data2: T2,
    data3: T3,
    data4: T4,
    data5: T5,
    data6: T6,
    data7: T7,
    data8: T8,
    data9: T9
) = Tuple9(data1, data2, data3, data4, data5, data6, data7, data8, data9)
