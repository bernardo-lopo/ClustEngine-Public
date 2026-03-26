package core.util

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class GetPropNames {
    companion object {
        fun Any.getPropNames(): String {
            return this::class.memberProperties.joinToString("\n") { prop ->
                @Suppress("UNCHECKED_CAST")
                val value = (prop as KProperty1<Any, Any?>).get(this)
                "${prop.name}: $value"
            }
        }
    }
}
