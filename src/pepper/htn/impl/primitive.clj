(ns pepper.htn.impl.primitive)

;; effects are USED DURING PLANNING
;; operator is the ACTUAL EXECUTION

(defn task [{:keys [name preconditions effects operator]}])

{:name :mine
 :preconditions [(fn [state] (not (:mining state)))]
 :effects [(fn [state] (assoc state :mining true))]
 :operator [(fn [state] (assoc state :mining true))]}