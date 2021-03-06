(ns googlesheets-sql-sync.oauth
  (:require
   [googlesheets-sql-sync.config :as config]
   [googlesheets-sql-sync.auth-file :as auth-file]
   [googlesheets-sql-sync.http :as http]
   [googlesheets-sql-sync.log :as log]
   [googlesheets-sql-sync.util :refer [query-string hostname]]))

(def scope "https://www.googleapis.com/auth/spreadsheets.readonly")

(def default-params-user {:scope         scope
                          :access_type   "offline"
                          :response_type "code"})

(def default-params-code {:grant_type "authorization_code"})

(def defaul-params-refresh {:grant_type "refresh_token"})

(defn url [google_credentials user-oauth-url]
  (let [q (->> (select-keys google_credentials [:client_id :redirect_uri])
               (merge default-params-user)
               query-string)]
    (str user-oauth-url "?" q)))

(defn local-redirect? [google_credentials]
  (-> google_credentials
      :redirect_uri
      hostname
      (= "localhost")))

(defn- get-access-token [{:keys [creds params server-oauth-url throttler]}]
  (let [p (merge (select-keys creds [:client_id :client_secret :redirect_uri])
                 params)]
    (-> (http/post server-oauth-url {:form-params p} throttler)
        (select-keys [:access_token :expires_in :refresh_token]))))

(defn handle-code
  "Get access token for auth code and update auth file"
  [{:as ctx :keys [code config-file auth-file]}]
  (log/info "Handling auth code")
  (try
    (let [auth (get-access-token (assoc ctx
                                        :creds  (:google_credentials (config/get config-file))
                                        :params (merge default-params-code {:code code})))]
      (auth-file/save auth-file auth))
    (catch Exception e (log/error "Errror handling code:" (.getMessage e)))))

(defn refresh-token
  "Refreshes access token, saves the new one and returns it."
  [{:as ctx :keys [config-file auth-file]}]
  (log/info "Refreshing access token")
  (let [{:keys [refresh_token] :as auth} (auth-file/get auth-file)]
    (if-not refresh_token
      (log/info "No refresh_token found")
      (try
        (let [new-auth (get-access-token (assoc ctx
                                                :creds (:google_credentials (config/get config-file))
                                                :params (merge defaul-params-refresh
                                                               {:refresh_token refresh_token})))]
          (auth-file/save auth-file (merge auth new-auth))
          (:access_token new-auth))
        (catch Exception e (do (log/error "Errror handling code:" (.getMessage e))
                               (log/info "Please reauthorize app")))))))
