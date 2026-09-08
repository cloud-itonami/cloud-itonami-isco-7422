(ns ictinstall.governor
  "ICTInstallGovernor — the independent safety/scope layer gating every
  site scheduling/logistics proposal an advisor may make for an ICT
  installer/servicer crew. The governor never dispatches hardware
  itself, never performs installation work, and never finalizes an
  installation-execution decision (e.g. deciding to proceed with a
  specific cable/equipment installation), authorizes or finalizes a
  network/electrical-compliance-clearance determination, or overrides
  a site safety officer's judgment — those are permanently out of
  this actor's scope and remain a site safety officer's exclusive
  judgment (README's 'Robotics premise': this actor coordinates SITE
  SCHEDULING/LOGISTICS ONLY — it never performs installation work
  itself). Modeled on cloud-itonami-isco-7412's elecmech.governor
  (closest domain shape — same physical-safety-domain, certification-
  gated technician-safety pattern).

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. installer provenance — the installer must be independently
                               verified/registered (including
                               certification status) before any
                               action.
    2. site provenance       — the installation site must be
                               independently verified/registered
                               before any action.
    3. no-actuation          — proposal :effect must be :propose (the
                               governor never dispatches hardware and
                               never performs installation work
                               itself; it only gates what the advisor
                               may coordinate).
    4. closed op-allowlist   — only :log-work-record,
                               :schedule-crew-operation,
                               :flag-safety-concern and
                               :coordinate-supply-order may ever be
                               proposed; anything else is refused.
    5. scope-excluded action — any proposal to directly finalize an
                               installation-execution decision (e.g.
                               deciding to proceed with a specific
                               cable/equipment installation), to
                               authorize or finalize a network/
                               electrical-compliance-clearance
                               determination, or to override a site
                               safety officer's judgment, is a hard,
                               permanent block (checked both against
                               the proposed :op and, defense-in-depth,
                               against the proposal's :rationale text
                               — matched as full finalization/
                               execution ACTION phrases such as
                               \"proceed with the installation\" /
                               \"authorize the network/electrical
                               compliance clearance\" / \"override the
                               site safety officer's judgment\", never
                               as bare nouns like \"installation\",
                               \"electrical\", \"network\", or \"safety
                               officer\", so the check can never
                               self-trip on the advisor's own routine
                               rationale text, e.g. \"logged work
                               record for installer …\" or \"scheduled
                               crew operation for network cabling
                               installation task …\" or \"…routed for
                               certified site safety officer review\"
                               — all three legitimately contain those
                               bare nouns but none is a finalization
                               action, and all are exercised by
                               `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off
  regardless of confidence):
    6. :op :flag-safety-concern (an electrical-shock-risk/working-at-
                               height/equipment-condition concern
                               always escalates to a human, never
                               auto-commits).
    7. :op :coordinate-supply-order above `supply-cost-threshold`.
    8. low confidence (< `confidence-floor`)."
  (:require [kotoba.lang.text :as str]
            [ictinstall.store :as store]))

(def confidence-floor 0.6)
(def supply-cost-threshold 2000)

(def allowed-ops
  #{:log-work-record :schedule-crew-operation
    :flag-safety-concern :coordinate-supply-order})

;; Defense-in-depth: none of these ops are ever in `allowed-ops`
;; above, so they are already refused by the closed-allowlist check
;; below; they are named again here — as explicit finalization/
;; execution ACTIONS, never bare nouns — so a future allowlist edit
;; cannot silently re-open this specific out-of-scope path without
;; also touching this list.
(def ^:private scope-excluded-ops
  #{:finalize-installation-execution-decision
    :authorize-installation-execution
    :proceed-with-installation
    :authorize-network-electrical-compliance-clearance
    :finalize-network-electrical-compliance-clearance-decision
    :override-site-safety-officer-judgment
    :bypass-site-safety-officer-judgment})

;; Full finalization/execution ACTION phrases only — never bare nouns
;; ("installation", "electrical", "network", "compliance", "safety
;; officer") — so this can never match inside the mock advisor's own
;; default rationale text (which legitimately contains those bare
;; nouns, e.g. "network cabling installation task" / "certified site
;; safety officer review"). See
;; `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
(def ^:private scope-excluded-phrases
  ["proceed with the installation"
   "finalize the installation-execution decision"
   "finalize the installation execution decision"
   "authorize the installation-execution"
   "authorize the installation execution"
   "authorize the network/electrical compliance clearance"
   "authorize the network electrical compliance clearance"
   "finalize the network/electrical compliance clearance decision"
   "finalize the network electrical compliance clearance decision"
   "override the site safety officer's judgment"
   "override the site safety officer judgment"
   "bypass the site safety officer's judgment"
   "bypass the site safety officer judgment"])

(defn- contains-excluded-phrase? [s]
  (let [s (str/lower (or s ""))]
    (boolean (some #(str/includes? s %) scope-excluded-phrases))))

(defn- hard-violations [proposal installer-record site-record]
  (let [{:keys [op rationale]} proposal]
    (cond-> []
      (nil? installer-record)
      (conj {:rule :no-installer
             :detail "未登録 installer への提案は不可（installer record は独立して検証・登録済み — certification status を含む — でなければならない）"})

      (nil? site-record)
      (conj {:rule :no-site
             :detail "未登録 site への提案は不可（site record は独立して検証・登録済みでなければならない）"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor は installation work を直接実行しない）"})

      (not (contains? allowed-ops op))
      (conj {:rule :unknown-op
             :detail (str op " は closed op-allowlist に無い — 提案不可")})

      (or (contains? scope-excluded-ops op) (contains-excluded-phrase? rationale))
      (conj {:rule :scope-excluded-action
             :detail "設置実行判断の確定・network/electrical compliance clearance 判定の許可/確定・site safety officer 判断の上書きは、この actor の権限外 — 常に永続ブロック"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `ictinstall.store/Store`. Pure — never mutates
  the store, never dispatches an installation operation."
  [request _context proposal store]
  (let [installer-record (store/installer store (:installer-id request))
        site-record (some->> (:site-id proposal) (store/site store))
        hard (hard-violations proposal installer-record site-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        supply-order-over-threshold?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (> (:cost proposal) supply-cost-threshold))
        always-risky? (or (= :flag-safety-concern (:op proposal))
                           supply-order-over-threshold?)]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
