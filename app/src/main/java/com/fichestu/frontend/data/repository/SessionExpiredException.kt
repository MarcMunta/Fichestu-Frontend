package com.fichestu.frontend.data.repository

class SessionExpiredException(
    message: String = "Sesión caducada. Vuelve a iniciar sesión."
) : Exception(message)
