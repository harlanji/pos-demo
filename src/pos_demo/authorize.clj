(ns pos-demo.authorize
  
  (:import [java.math BigDecimal RoundingMode])

  (:import [net.authorize Environment])
  (:import [net.authorize.api.contract.v1
              MerchantAuthenticationType
              PaymentType CreditCardType
              TransactionRequestType
              TransactionTypeEnum
              CreateTransactionRequest
              CreateTransactionResponse
              MessageTypeEnum 
              ARBCreateSubscriptionRequest
              ARBCreateSubscriptionResponse
              CreateCustomerProfileRequest
              CreateCustomerProfileResponse
              CustomerPaymentProfileType
              CustomerTypeEnum
              CustomerProfileType
              CreateCustomerProfileRequest
              ValidationModeEnum
              CreateCustomerProfileResponse
              MessageTypeEnum
              ANetApiResponse
            ])
  (:import [net.authorize.api.controller.base
             ApiOperationBase])
  (:import [net.authorize.api.controller 
             CreateTransactionController
             CreateCustomerProfileController
             ARBCreateSubscriptionController])
             
             
(:import [net.authorize.api.contract.v1
            PaymentScheduleType
            PaymentScheduleType$Interval
            ARBSubscriptionUnitEnum
            ARBSubscriptionType
            CustomerProfileIdType
            CustomerAddressType
  ])
  
  ;; 
  (:import [javax.xml.datatype DatatypeFactory XMLGregorianCalendar])  
  )


(defn init [api-login-id transaction-key]
; Common code to set for all requests
  (ApiOperationBase/setEnvironment Environment/SANDBOX)
  
  (let [merchant-authentication-type (MerchantAuthenticationType.)]
    (.setName merchant-authentication-type api-login-id)
    (.setTransactionKey merchant-authentication-type transaction-key)
    
    (ApiOperationBase/setMerchantAuthentication merchant-authentication-type)
    
     (println "Authorize initialized.")))


(defn create-test-payment-request [card-number card-exp amount]
  (let [payment-type (PaymentType.)
        credit-card (CreditCardType.)]
        (.setCardNumber credit-card card-number)
        (.setExpirationDate credit-card card-exp)
        (.setCreditCard payment-type credit-card)
        
        (let [txn-request (TransactionRequestType.)]
          (.setTransactionType txn-request (.value TransactionTypeEnum/AUTH_CAPTURE_TRANSACTION))
          (.setPayment txn-request payment-type)
          (.setAmount txn-request (BigDecimal. amount))
          
          txn-request)
        

))



(defn send-payment-request [txn-request]
  (let [api-request (CreateTransactionRequest.)]
    (.setTransactionRequest api-request txn-request)
    (let [controller (CreateTransactionController. api-request)
          exec-result (.executeWithApiResponse controller)]
      
      (println "Exec result: " exec-result)
      
      ; is execute sync?
      (let [response (.getApiResponse controller)]
        (if response
          (if (= (.getResultCode (.getMessages response)) MessageTypeEnum/OK)
            (let [result (.getTransactionResponse response)]
              (if (.equals (.getResponseCode result) "1")
                (println "SUCCESS. == " (.getResponseCode result) " // " (.getAuthCode result) " // " (.getTransId result))
                (println "Failed transaction a: " (.getResponseCode result))))
            (println "Failed response. Code=" (.getCode (.get (.getMessage (.getMessages response)) 0))
              "Text=" (.getText (.get (.getMessage (.getMessages response)) 0))))
          (println "No API response."))))))