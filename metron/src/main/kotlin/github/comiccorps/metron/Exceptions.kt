package github.comiccorps.metron

open class ServiceException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class AuthenticationException(message: String? = null, cause: Throwable? = null) : ServiceException(message, cause)
