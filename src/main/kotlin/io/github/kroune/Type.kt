package io.github.kroune

sealed class PublishType {
    data object Release : PublishType()

    data object PreRelease : PublishType()

    data object Testing : PublishType()

    data object Other : PublishType()

    fun encodeToString(): String {
        return this.javaClass.simpleName
    }
}

private val nameResolver =
    PublishType::class.nestedClasses.mapNotNull { it.objectInstance as? PublishType }
        .associateBy { it.javaClass.simpleName }

fun String.decodeToPublishType(): PublishType? {
    return nameResolver[this]
}