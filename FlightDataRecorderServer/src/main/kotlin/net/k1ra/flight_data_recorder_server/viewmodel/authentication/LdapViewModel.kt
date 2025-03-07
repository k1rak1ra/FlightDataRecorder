package net.k1ra.flight_data_recorder_server.viewmodel.authentication

import kotlinx.coroutines.delay
import net.k1ra.flight_data_recorder.model.authentication.ClientUserData
import net.k1ra.flight_data_recorder.model.authentication.LoginRequest
import net.k1ra.flight_data_recorder_server.utils.Constants
import org.ldaptive.BindConnectionInitializer
import org.ldaptive.ConnectionConfig
import org.ldaptive.Credential
import org.ldaptive.DefaultConnectionFactory
import org.ldaptive.SearchOperation
import org.ldaptive.SearchRequest
import org.ldaptive.auth.AuthenticationRequest
import org.ldaptive.auth.Authenticator
import org.ldaptive.auth.SearchDnResolver
import org.ldaptive.auth.SimpleBindAuthenticationHandler
import org.ldaptive.handler.LdapEntryHandler
import org.ldaptive.handler.ResultHandler
import java.time.Duration

object LdapViewModel {
    private val connection: ConnectionConfig? = if (Constants.LDAP_SERVER.isNullOrEmpty()) {
        null
    } else if (Constants.LDAP_BIND_USER.isNullOrEmpty()) {
        ConnectionConfig.builder()
            .url(Constants.LDAP_SERVER)
            .useStartTLS(Constants.LDAP_TLS_ENABLED)
            .connectTimeout(Duration.ofSeconds(30))
            .build()
    } else {
        ConnectionConfig.builder()
            .url(Constants.LDAP_SERVER)
            .useStartTLS(Constants.LDAP_TLS_ENABLED)
            .connectionInitializers(
                BindConnectionInitializer.builder()
                    .dn(Constants.LDAP_BIND_USER)
                    .credential(Constants.LDAP_BIND_PASSWORD)
                    .build()
            )
            .connectTimeout(Duration.ofSeconds(30))
            .build()
    }

    suspend fun isUserAdmin(user: String) : Boolean {
        var done = false
        var userIsAdmin = false

        try {
            SearchOperation.builder()
                .factory(DefaultConnectionFactory(connection))
                .onEntry(LdapEntryHandler {
                    if (it.getAttribute("cn").stringValue == Constants.LDAP_ADMIN_GROUP_NAME)
                        userIsAdmin = true
                    it
                })
                .onResult(ResultHandler {
                    done = true
                }).build()
                .send(
                    SearchRequest.builder()
                    .dn(Constants.LDAP_GROUP_DN)
                    .filter(Constants.LDAP_GROUP_FILTER!!.replace("{user}", user))
                    .returnAttributes("cn")
                    .build()
                )
        } catch (e: Exception) {
            e.printStackTrace()
            done = true
        }

        //Wait for request to complete, since we want to use direct returns and not callbacks
        while (!done)
            delay(100)

        return userIsAdmin
    }

    suspend fun doLogin(request: LoginRequest) : ClientUserData? {
        var doneUserSearch = false

        try {
            val authResolver = SearchDnResolver.builder()
                .factory(DefaultConnectionFactory(connection))
                .dn(Constants.LDAP_USER_DN)
                .filter(Constants.LDAP_USER_FILTER!!.replace("{user}", request.emailOrUsername))
                .build()

            val authHandler = SimpleBindAuthenticationHandler(DefaultConnectionFactory(connection))
            val auth = Authenticator(authResolver, authHandler)
            val response = auth.authenticate(AuthenticationRequest(request.emailOrUsername, Credential(request.password)))

            if (response.isSuccess) {
                val attributes: MutableMap<String, String> = mutableMapOf()
                SearchOperation.builder()
                    .factory(DefaultConnectionFactory(connection))
                    .onEntry(LdapEntryHandler {
                        it.attributes.forEach {
                            attributes[it.name] = it.stringValue
                        }
                        it
                    }).onResult(ResultHandler {
                        doneUserSearch = true
                    }).build()
                    .send(SearchRequest.builder()
                        .dn(Constants.LDAP_USER_DN)
                        .filter(Constants.LDAP_USER_FILTER.replace("{user}", request.emailOrUsername))
                        .build()
                    )

                while (doneUserSearch)
                    delay(100)

                val userIsAdmin  = isUserAdmin(request.emailOrUsername)
                val ldapUid = "LDAP-${attributes[Constants.LDAP_USER_UID_ATTRIBUTE]!!}"

                return UserViewModel.processLdapUser(ldapUid, userIsAdmin, request.emailOrUsername, attributes)
            } else {
                return null
            }

        } catch (e: Exception) {
            return null
        }
    }
}