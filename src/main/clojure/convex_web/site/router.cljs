(ns convex-web.site.router
  (:require [convex-web.site.runtime :as runtime :refer [disp sub]]
            [convex-web.site.backend :as backend]
            [convex-web.site.session :as session]
            [convex-web.site.stack :as stack]

            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [re-frame.core :as re-frame]))

(defn route [db]
  (:site/route db))

(defn route-spec [db]
  (get-in db [:site/route :route/match :data :spec]))

(defn route-state [db]
  (get-in db [:site/route :route/state]))

(defn route-controllers [db]
  (get-in db [:site/route :route/match :controllers]))

;; ---

(re-frame/reg-fx :push-history-state
  (fn [route]
    (apply rfe/push-state route)))

(re-frame/reg-sub :router/?route
  (fn [db _]
    (route db)))

(re-frame/reg-sub :router/?route-spec
  (fn [db _]
    (route-spec db)))

(re-frame/reg-sub :router/?route-state
  (fn [db _]
    (route-state db)))

(re-frame/reg-event-fx :router/!push
  (fn [_ [_ & route]]
    {:push-history-state route}))

(re-frame/reg-event-fx :router/!navigate
  (fn [{:keys [db]} [_ match history]]
    (if match
      (let [controllers (route-controllers db)

            controllers (rfc/apply-controllers controllers match)

            new-route (assoc match :controllers controllers)]
        {:db (update db :site/route merge #:route {:match new-route
                                                   :history history})})
      {:db (assoc db :site/route #:route {})})))

;; ---

(defn ?route []
  (sub :router/?route))

;; ----

(defonce routes
  ["/"
   {:controllers
    [{:identity identity
      :start (fn [_]
               (runtime/with-db
                 (fn [{:site/keys [session]}]
                   ;; Create Session - if there isn't one already
                   (when-not session
                     (backend/GET-session
                       {:handler
                        (fn [session]
                          (session/create session))})))))}]}


   ;; Welcome
   ;; ==============
   [""
    {:name :route-name/welcome
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/welcome {:reset? true}))}]}]


   ;; Create account
   ;; ==============
   ["create-account"
    {:name :route-name/create-account
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/create-account {:reset? true}))}]}]


   ;; Sandbox
   ;; ==============
   ["sandbox"
    {:name :route-name/sandbox
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/repl {:reset? true}))}]}]


   ;; My Account (account details)
   ;; ==============
   ["my-account"
    {:name :route-name/my-account
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/my-account {:reset? true
                                                 :state
                                                 {:convex-web/account
                                                  {:convex-web.account/address (session/?active-address)}}}))}]}]

   ;; Wallet
   ;; ==============
   ["wallet"
    {:name :route-name/wallet
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/wallet {:reset? true}))}]}]


   ;; Faucet
   ;; ==============
   ["faucet"
    {:name :route-name/faucet
     :controllers
     [{:identity identity
       :start (fn [_]
                (stack/push :page.id/faucet {:reset? true
                                             :state {:convex-web/faucet {:convex-web.faucet/amount 1000000}}}))}]}]

   ;; Explorer
   ;; ==============
   ["explorer"
    [""
     {:name :route-name/explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/explorer {:reset? true}))}]}]

    ["/accounts"
     {:name :route-name/accounts-explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/accounts-range-explorer {:reset? true
                                                               :state {:ajax/status :ajax.status/pending}}))}]}]

    ["/accounts/:address"
     {:name :route-name/account-explorer
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [address (get-in match [:parameters :path :address])]
                   (stack/push :page.id/account-explorer {:reset? true
                                                          :state
                                                          {:ajax/status :ajax.status/pending
                                                           :convex-web/account {:convex-web.account/address address}}})))}]}]

    ["/blocks"
     {:name :route-name/block-coll-explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/blocks-range-explorer {:reset? true}))}]}]

    ["/blocks/:index"
     {:name :route-name/block-explorer
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [index (js/parseInt (get-in match [:parameters :path :index]))]
                   (stack/push :page.id/block-explorer {:reset? true
                                                        :state {:convex-web/block {:convex-web.block/index index}}})))}]}]

    ["/peers"
     {:name :route-name/peers-explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/peers-explorer {:reset? true}))}]}]

    ["/transactions"
     {:name :route-name/transactions-explorer
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/transactions-range-explorer {:reset? true}))}]}]]


   ;; Documentation
   ;; ==============
   ["documentation"
    [""
     {:name :route-name/documentation
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/documentation {:reset? true}))}]}]


    ["/reference"
     {:name :route-name/documentation-reference
      :controllers
      [{:identity identity
        :start (fn [match]
                 (let [symbol (get-in match [:parameters :query :symbol])]
                   (stack/push :page.id/documentation-reference (merge {:reset? true}
                                                                       (when symbol
                                                                         {:state {:symbol symbol}})))))}]}]

    ["/getting-started"
     {:name :route-name/documentation-getting-started
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/documentation-getting-started {:reset? true}))}]}]

    ["/tutorial"
     {:name :route-name/documentation-tutorial
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/documentation-tutorial {:reset? true}))}]}]

    ["/advanced-topics"
     {:name :route-name/advanced-topics
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/advanced-topics {:reset? true}))}]}]]


   ;; About
   ;; ==============
   ["about"
    [""
     {:name :route-name/about
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/about {:reset? true}))}]}]

    ["/vision"
     {:name :route-name/vision
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/vision {:reset? true}))}]}]

    ["/faq"
     {:name :route-name/faq
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/faq {:reset? true}))}]}]

    ["/concepts"
     {:name :route-name/concepts
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/concepts {:reset? true}))}]}]

    ["/white-paper"
     {:name :route-name/white-paper
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/white-paper {:reset? true}))}]}]

    ["/get-involved"
     {:name :route-name/get-involved
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/under-construction {:reset? true}))}]}]

    ["/roadmap"
     {:name :route-name/roadmap
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/under-construction {:reset? true}))}]}]

    ["/convex-foundation"
     {:name :route-name/convex-foundation
      :controllers
      [{:start (fn [_]
                 (stack/push :page.id/under-construction {:reset? true}))}]}]]])

(def router
  (rf/router routes))

(defn start []
  (rfe/start! router (fn [match history]
                       (disp :router/!navigate match history))
              {:use-fragment true}))

(defn route-name [route]
  (get-in route [:data :name]))
