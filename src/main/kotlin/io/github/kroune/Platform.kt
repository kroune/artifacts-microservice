package io.github.kroune

sealed class PlatformType {
    data object WindowsExe : PlatformType()

    data object WindowsMsi : PlatformType()

    data object LinuxDeb : PlatformType()

    data object LinuxRpm : PlatformType()

    data object MacosPkg : PlatformType()

    data object MacosDmg : PlatformType()

    data object Web : PlatformType()

    data object Ios : PlatformType()

    data object Jar : PlatformType()

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