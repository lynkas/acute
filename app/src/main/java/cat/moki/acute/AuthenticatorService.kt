package cat.moki.acute

import android.accounts.AccountManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {

    private var authenticator: Authenticator? = null

    override fun onBind(intent: Intent): IBinder? {
        var binder: IBinder? = null
        if (intent.action == AccountManager.ACTION_AUTHENTICATOR_INTENT) {
            binder = getAuthenticator().getIBinder()
        }
        return binder
    }

    private fun getAuthenticator(): Authenticator {
        if (null == authenticator) {
            authenticator = Authenticator(this)
        }
        return authenticator!!
    }

}