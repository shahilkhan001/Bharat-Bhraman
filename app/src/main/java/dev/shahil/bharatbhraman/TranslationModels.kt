package dev.shahil.bharatbhraman


data class MyMemoryResponse(
    val responseData: ResponseData?
)

data class ResponseData(
    val translatedText: String?
)