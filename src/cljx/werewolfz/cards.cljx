(ns werewolfz.cards)

;; -----
;; utils
;; -----

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
   :minion {:victory-condition village-team-wins?}
   :villager {:victory-condition village-team-wins?}
   :seer {:victory-condition village-team-wins?}
   :robber {:victory-condition village-team-wins?}
   :troublemaker {:victory-condition village-team-wins?}
   :tanner {:victory-condition village-team-wins?}
   :drunk {:victory-condition village-team-wins?}
   :hunter {:victory-condition village-team-wins?}
   :mason {:victory-condition village-team-wins?}
   :insomniac {:victory-condition village-team-wins?}
   :doppelganger {:victory-condition village-team-wins?}})

(defn basic-setup
  [num-players])
