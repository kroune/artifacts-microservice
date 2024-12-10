package io.github.kroune

sealed class PlatformType {
    data object Windows : PlatformType()

    data object Linux : PlatformType()

    data object Macos : PlatformType()

    data object Web : PlatformType()

    data object Android : PlatformType()

    fun encodeToString(): String {
        return this.javaClass.simpleName
    }
}

private val nameResolver =
    PlatformType::class.nestedClasses.mapNotNull { it.objectInstance as? PlatformType }
        .associateBy { it.javaClass.simpleName }

fun String.decodeToPlatformType(): PlatformType? {
    return nameResolver[this]
}