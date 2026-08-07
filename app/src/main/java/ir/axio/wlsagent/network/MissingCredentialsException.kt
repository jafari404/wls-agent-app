package ir.axio.wlsagent.network

import java.io.IOException

/**
 * Thrown by the auth interceptor when no credentials are stored, so that the request fails loudly
 * instead of being sent with an empty Authorization header.
 */
class MissingCredentialsException : IOException("No stored WordPress credentials")
