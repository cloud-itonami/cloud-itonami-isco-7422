(ns ictinstall.store
  "SSoT for the ISCO-08 7422 Information and Communications Technology
  Installers and Servicers site scheduling/logistics coordination
  actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md Actors
  section; README's 'Robotics premise' — a site scheduling/logistics
  coordination robot performs installation-job scheduling,
  installation-job/progress record logging and cabling/networking-
  equipment supply-order coordination for an ICT installer/servicer
  crew under this advisor/governor pair, which never dispatches
  hardware itself, never performs installation work itself, and never
  finalizes an installation-execution decision, a network/electrical-
  compliance-clearance determination, or overrides a site safety
  officer's judgment — those remain a site safety officer's exclusive
  judgment). Modeled on cloud-itonami-isco-7412's elecmech.store
  (closest domain shape — same physical-safety-domain, certification-
  gated technician-safety pattern).

  Domain:

    installer — a registered ICT installer/servicer
                {:installer-id :name :certified?}. `:certified?` is
                informational registered data (the installer's
                certification status as recorded at registration
                time) — the governor's provenance check only requires
                the installer record to exist (independently
                verified/registered before any action); it never lets
                a proposal override or bypass the certification
                requirement itself, and never lets a proposal override
                a site safety officer's judgment (see
                ictinstall.governor's scope-excluded-action rule).
    site      — a registered installation site {:site-id :name
                :max-supply-cost number}. `:max-supply-cost` is an
                informational registered ceiling used only to decide
                whether a `:coordinate-supply-order` proposal escalates
                to human sign-off (the governor never blocks a
                within-threshold order outright; it only decides
                commit vs. escalate).
    record    — a committed operating record (a logged installation-
                job/progress entry, a scheduled crew operation, a
                flagged safety concern, or a coordinated supply order)
                — written ONLY via commit-record!.
    ledger    — append-only audit trail, commit or hold.")

(defprotocol Store
  (installer [s installer-id])
  (site [s site-id])
  (records-of [s installer-id])
  (ledger [s])
  (register-installer! [s inst])
  (register-site! [s st])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (installer [_ installer-id] (get-in @a [:installers installer-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ installer-id] (filter #(= installer-id (:installer-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-installer! [s inst]
    (swap! a assoc-in [:installers (:installer-id inst)] inst) s)
  (register-site! [s st]
    (swap! a assoc-in [:sites (:site-id st)] st) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:installers {} :sites {} :records [] :ledger []}
                                    seed)))))
