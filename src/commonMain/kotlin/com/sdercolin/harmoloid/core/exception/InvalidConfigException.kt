package com.sdercolin.harmoloid.core.exception

abstract class InvalidConfigException(message: String?) : Exception(
    "Invalid config is found. $message"
) {

    class ValueOutOfRange(
        val parameterName: String,
        val value: Number,
        val correctRangeDescription: String
    ) : InvalidConfigException(
        "Parameter \"$parameterName\" is set to $value, but should be in the range of $correctRangeDescription."
    )

    class IncorrectListSize(
        val parameterName: String,
        val size: Int,
        val correctRangeDescription: String
    ) : InvalidConfigException(
        "Parameter \"$parameterName\" has an incorrect size $size. " +
                "It should be in the range of $correctRangeDescription."
    )

    class ListContentValueOutOfRange(
        val parameterName: String,
        val index: Int,
        val value: Number,
        val correctRangeDescription: String
    ) : InvalidConfigException(
        "The content of parameter \"$parameterName\" (index: $index) is set to $value," +
                " but should be in the range of $correctRangeDescription."
    )
}
