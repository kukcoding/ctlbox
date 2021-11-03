@file:Suppress("unused")

package myapp.inject

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import myapp.common.json.MyMoshiDateAdapters
import java.util.*
import javax.inject.Singleton

// 여기글 읽어보자
// https://engineering.kitchenstories.io/data-classes-and-parsing-json-a-story-about-converting-models-to-kotlin-caf8a599df9e

@Module
@InstallIn(SingletonComponent::class)
object JsonModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            // PolymorphicJsonAdapterFactory가 앞에 있어야 된다.(순서가 중요함)
//            .add(
//                PolymorphicJsonAdapterFactory.of(PushPayload::class.java, "pushType")
//                    .withSubtype(PushPayload.BibleCardFavor::class.java, PushType.BIBLE_CARD_FAVOR.name)
//            )
            .add(KotlinJsonAdapterFactory())
            .add(MyMoshiDateAdapters.InstantAdapter)
            .add(MyMoshiDateAdapters.LocalDateAdapter)
            .add(MyMoshiDateAdapters.LocalDateTimeAdapter)
            .add(MyMoshiDateAdapters.OffsetDateTimeAdapter)
            .add(MyMoshiDateAdapters.ZonedDateTimeAdapter)
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()
    }
}



