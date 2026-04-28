package com.fichestu.frontend.data.repository

class SessionExpiredException(
    message: String = "Sesion caducada. Vuelve a iniciar sesion."
) : Exception(message)
