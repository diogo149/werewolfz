(ns werewolfz.logic.game)

;; -----
;; utils
;; -----
(def alphanumeric-chars
  (map char (concat (range 66 91) ;; A-Z
                    (range 97 123)))) ;; a-z


(defn random-char []
  (nth alphanumeric-chars (rand (count alphanumeric-chars))))

(defn random-alphanumeric-string
  []
  (apply str (take 10 (repeatedly random-char))))


;; for debugging
(defn ?
  [thing]
  (println thing)
  thing)

(defn werewolf?
  [role]
  (= role :werewolf))



;; ------------------
;; victory conditions
;; ------------------

(defn village-team-wins?
  [{:keys [all-roles killed-roles]}]
  (or (some werewolf? killed-roles)
      (and (not-any? werewolf? all-roles)
           (empty? killed-roles))))

(defn werewolf-team-wins?
  [{:keys [all-roles killed-roles]}]
  (and (not-any? werewolf? killed-roles)
       (some werewolf? all-roles)))

(defn tanner-wins?
  [{:keys [killed-roles died?]}]
  died?)

;; -----
;; cards
;; -----

(def all-cards
  {:werewolf {:victory-condition village-team-wins?}
;;   :minion {:victory-condition village-team-wins?}
   :villager {:victory-condition village-team-wins?}
   :seer {:victory-condition village-team-wins?}
   :robber {:victory-condition village-team-wins?}
 ;;  :troublemaker {:victory-condition village-team-wins?}
 ;;  :tanner {:victory-condition village-team-wins?}
 ;;  :drunk {:victory-condition village-team-wins?}
 ;;  :hunter {:victory-condition village-team-wins?}
 ;;  :mason {:victory-condition village-team-wins?}
 ;;  :insomniac {:victory-condition village-team-wins?}
  ;; :doppelganger {:victory-condition village-team-wins?}
   })

;; ------
;; phases
;; ------
(def phases
  {:werewolf
   {:next-phase :seer}
   :seer
   {:next-phase :robber}
   :robber
   {:next-phase :done}})

(defn choose-roles
  [num-players]
  {:pre [(> num-players 2)]}
  (shuffle (concat '(:seer :werewolf :werewolf :robber)
                   (repeat (- num-players 1) :villager))))

(defn basic-setup
  ([num-players]
     (basic-setup
      num-players
      (take num-players (repeatedly random-alphanumeric-string))))
  ([num-players player-names]
     (let [possible-roles (choose-roles num-players)
           starting-roles (zipmap player-names (drop 3 possible-roles))]
       {:next-phase :werewolf
        :middle-roles (take 3 possible-roles)
        :starting-roles starting-roles
        :roles starting-roles
        :inputs (zipmap player-names (repeat num-players nil))
        :outputs (zipmap player-names (repeat num-players nil))})))



(defn get-players-by-starting-role
  [role-kw {:keys [starting-roles] :as game-state}]
  (map key (filter #(= (val %) role-kw) starting-roles)))




;; ------------
;; Phase ready?
;; ------------

(defn basic-phase-ready
  "Given a role keyword checks if there is a player with that starting role,
   and checks if they've given input yet. Assumes only one exists."
  [rolekw {:keys [inputs starting-roles] :as game-state}]
  (let [player (first (get-players-by-starting-role rolekw game-state))
        {:keys [input-type] :as input} (get inputs player)]
    (if-not player
      true
      (if (= input-type rolekw)
        true
        false))))

(defmulti phase-ready?
  "Checks if the necessary inputs are available
   for the next phase to be executed."
  :next-phase)

(defmethod phase-ready? :werewolf
  [game-state]
  true)

(defmethod phase-ready? :seer
  [game-state]
  (basic-phase-ready :seer game-state))

(defmethod phase-ready? :robber
  [game-state]
  (basic-phase-ready :robber game-state))

;; ---------
;; run-phase
;; ---------

;; TODO: Call this only one place. No reason to call it in every run-phase.
(defn move-to-next-phase
  [game-state]
  (assoc game-state
    :next-phase (-> game-state
                    :next-phase
                    phases
                    :next-phase)))

(defmulti run-phase
  :next-phase)

(defmethod run-phase :werewolf
  ;; TODO: Lone wolf rule.
  [game-state]
  (let [werewolves (get-players-by-starting-role :werewolf game-state)]
    (move-to-next-phase
     (reduce
      #(assoc-in %1 [:outputs %2] {:werewolves werewolves})
      game-state
      werewolves))))

(defmethod run-phase :seer
  [{:keys [inputs middle-roles roles] :as game-state}]
  (let [seer (first (get-players-by-starting-role :seer game-state))
        {:keys [seer-choice seer-person-to-see seer-middle-indexes]
         :as seer-input} (when seer
                           (get inputs seer))
        seer-output (when seer (merge {:output-type :seer}
                                      (if (= seer-choice "middle")
                                        (zipmap seer-middle-indexes
                                                (map #(get middle-roles %)
                                                     seer-middle-indexes))
                                        (hash-map seer-person-to-see
                                                  (roles seer-person-to-see)))))]
    (move-to-next-phase
     (if seer
       (assoc-in game-state [:outputs seer] seer-output)
       game-state))))

(defmethod run-phase :robber
  [{:keys [inputs middle-roles roles] :as game-state}]
  (let [robber (first (get-players-by-starting-role :robber game-state))
        {:keys [robbed-person]
         :as robber-input} (when robber
                             (get inputs robber))
        robbed-role (roles robbed-person)
        robber-output (when robber {:output-type :robber
                                    robbed-person robbed-role})]
    (move-to-next-phase
     (if robber
       (-> game-state
           (assoc-in [:outputs robber] robber-output )
           (assoc-in [:roles robber] robbed-role)
           (assoc-in [:roles robbed-person] :robber))
       game-state))))


;; ----------
;; User Input
;; ----------

(defn add-input
  [game-state user input]
  (assoc-in game-state [:inputs user] input))

(defn get-input-for-role
  [role-kw]
  (condp = role-kw
    :seer :seer-input
    :robber :robber-input
    nil))

(defn get-required-inputs
  [game-state]
  (->> game-state
      :starting-roles
      (map (fn [[k v]] [k (get-input-for-role v)]))
      (remove #(nil? (second %)))
      (into {})))

;; ----------
;; for repl use
;; ----------

(def game (atom (basic-setup 5)))

(defn phase-ready-atom [] (phase-ready? @game))

(defn run-phase-atom [] (reset! game (run-phase @game)))
