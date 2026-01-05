# funzionamento atteso: 

1. alla creazione il lavoro avrà status = SCHEDULED,
2. il tecnico lo prende in carico, il lavoro passa da SCHEDULED a IN_PROGRESS. 
   - se il lavoro preso in carico ha un ticket associato lo stato del ticket passa da OPEN a IN_PROGRESS (p.s. il ticket alla creazione ha status OPEN)
3. il tecnico segna il lavoro come completato, lo status passa a CLOSED 
   - se il lavoro ha un ticket associato il ticket passa a RESOLVED 
4. amministrazione segna il lavoro come fatturato, il lavoro passa a INVOICED 
   - se c'è un ticket associato il ticket passa a CLOSED

# ANALISI DEL PIANO WORKFLOW

## COSA ESISTE GIÀ
- Ticket Status:
  - Enum TicketStatus già presente con tutti gli stati necessari: OPEN, IN_PROGRESS, RESOLVED, CLOSED
  - Campo status nell'entità Ticket
  - Endpoint PATCH /tickets/{id} per aggiornare lo status
- Work Endpoints
  - PATCH /works/{id}/close - segna come completato (autorizzazione: tecnico)
  - PATCH /works/{id}/invoice - segna come fatturato (autorizzazione: amministrativo)
  - Dashboard con conteggi per lavori completati/pendenti
- Relazione Ticket-Work
  - Campo ticket nell'entità Work (relazione Many-to-One)

## COSA MANCA
- Status Enum per Work: l'entità Work usa attualmente boolean flags (completed, invoiced) invece di un enum di stati
- Esiste un enum WorkStatus NON UTILIZZATO in src/main/java/marcomanfrin/atixbackend/enums/WorkStatus.java con: SCHEDULED, IN_PROGRESS, COMPLETED
  - Manca lo stato INVOICED nell'enum (o va gestito separatamente)
- Endpoint "Presa in Carico"
  - Non esiste endpoint per la transizione SCHEDULED → IN_PROGRESS
- Logica di Transizione Automatica Ticket: 
  - Non c'è logica che aggiorna automaticamente lo status del ticket quando cambia lo status del lavoro
  - Gli endpoint closeWork e invoiceWork non toccano il ticket associato
- Validazione Workflow
  - Non esiste una state machine o validazione delle transizioni

Attualmente si può passare da qualsiasi stato a qualsiasi stato senza controlli

# CONFRONTO: PIANO vs IMPLEMENTAZIONE ATTUALE

| Fase            | Piano Proposto                                     | Implementazione Attuale                                     |
|-----------------|----------------------------------------------------|-------------------------------------------------------------|
| Creazione       | status = SCHEDULED                                 | completed = false, invoiced = false                         |
| Presa in carico | SCHEDULED → IN_PROGRESS+ ticket OPEN → IN_PROGRESS | ❌ Non esiste endpoint                                      |
| Completamento   | IN_PROGRESS → CLOSED+ ticket → RESOLVED            | closeWork() imposta completed = true❌ Non aggiorna ticket  |
| Fatturazione    | CLOSED → INVOICED+ ticket → CLOSED                 | invoiceWork() imposta invoiced = true❌ Non aggiorna ticket |

# MODIFICHE NECESSARIE

Per implementare il piano proposto:

Migrazione da boolean a enum (Work.java)
- Sostituire completed e invoiced con campo WorkStatus status
- Aggiungere stato INVOICED all'enum oppure mantenere invoiced come flag separato
- Aggiornare query e repository

Nuovo endpoint presa in carico (WorksController.java, WorkService.java)
PATCH /works/{id}/start
// SCHEDULED → IN_PROGRESS
// + aggiorna ticket se presente: OPEN → IN_PROGRESS

Aggiornare endpoint esistenti per gestire ticket
- closeWork(): CLOSED + aggiorna ticket a RESOLVED
- invoiceWork(): INVOICED + aggiorna ticket a CLOSED

Validazione state machine (WorkService.java)
- Verificare transizioni valide prima di cambiar stato
- SCHEDULED → IN_PROGRESS (solo questa)
- IN_PROGRESS → CLOSED (solo questa)
- CLOSED → INVOICED (solo questa)

Frontend tabs (già supportate dal backend)
- "lavori programmati": filtrare per status = SCHEDULED
- "lavori aperti": filtrare per status = IN_PROGRESS
- "lavori chiusi": filtrare per status IN (CLOSED, INVOICED)

⚠️ CONSIDERAZIONI
Gestione dello stato INVOICED:
Opzione A: Aggiungere INVOICED all'enum WorkStatus → workflow lineare semplice
Opzione B: Mantenere invoiced come boolean separato → permette "CLOSED ma non fatturato"

Backward compatibility: elimina i booleani e non lasciare il vecchio codicre
Lavori esistenti hanno solo completed e invoiced booleani
Serve migrazione dati o logica di conversione:
completed=false, invoiced=false → SCHEDULED o IN_PROGRESS?
completed=true, invoiced=false → CLOSED
completed=true, invoiced=true → INVOICED
Sincronizzazione Ticket:
Verificare se un ticket può essere associato a più lavori -> NO
Gestire caso in cui ticket viene aggiornato manualmente (bypass del workflow automatico)

Stati Workflow Lavoro

SCHEDULED - Lavoro programmato (stato iniziale)
IN_PROGRESS - Lavoro preso in carico
CLOSED - Lavoro completato
INVOICED - Lavoro fatturato (stato finale)

Transizioni Permissive Mode
SCHEDULED ─────→ IN_PROGRESS ─────→ CLOSED ─────→ INVOICED
│                 │                │
└─────────────────┘                │
(rollback errore)                  │
│
┌──────────────────┘
↓
(riapertura per fix)
Sincronizzazione Ticket Automatica
Azione su WorkWork StatusTicket StatusCreazioneSCHEDULEDOPENPresa in caricoIN_PROGRESSIN_PROGRESSCompletamentoCLOSEDRESOLVEDFatturazioneINVOICEDCLOSED
Nuovi Endpoint API

PATCH /works/{id}/start - Prende in carico
PATCH /works/{id}/close - Completa
PATCH /works/{id}/invoice - Fattura
PATCH /works/{id}/reopen - Riapre
PATCH /works/{id}/force-status - Forza stato (OWNER only)

Nuovi Filtri

?status=IN_PROGRESS - Filtra per singolo stato
?statuses=CLOSED,INVOICED - Filtra per più stati

Nuovi File (da creare)
bash# Copia questi file nelle rispettive directory del tuo progetto

# Exception
backend-changes/InvalidWorkflowTransitionException.java
→ src/main/java/marcomanfrin/atixbackend/exception/

# Service
backend-changes/WorkStateMachine.java
→ src/main/java/marcomanfrin/atixbackend/service/

# Migration
backend-changes/V2__add_work_status_workflow.sql
→ src/main/resources/db/migration/
File da Sostituire/Aggiornare
bash# Questi file esistono già - vanno MODIFICATI

# Enum
backend-changes/WorkStatus.java
→ src/main/java/marcomanfrin/atixbackend/enums/
(SOSTITUISCI - aggiunge INVOICED)

# Entità
backend-changes/Work.java
backend-changes/Ticket.java
→ src/main/java/marcomanfrin/atixbackend/entity/
(CONFRONTA E AGGIORNA - aggiungi campi status tracking)

# Service
backend-changes/WorkService.java
→ src/main/java/marcomanfrin/atixbackend/service/
(CONFRONTA E AGGIORNA - aggiungi metodi workflow)

# Controller
backend-changes/WorksController.java
→ src/main/java/marcomanfrin/atixbackend/controller/
(CONFRONTA E AGGIORNA - aggiungi endpoint workflow)

# DTO
backend-changes/WorkFilterDTO.java
→ src/main/java/marcomanfrin/atixbackend/dto/work/
(CONFRONTA E AGGIORNA - aggiungi filtri status)

# Specification
backend-changes/WorkSpecification.java
→ src/main/java/marcomanfrin/atixbackend/specification/
(CONFRONTA E AGGIORNA - aggiungi logic filtri status)

Autorizzazioni per Endpoint
EndpointAutorizzazione Richiesta/startUSER, ADMIN, OWNER/closeTECHNICIAN, OWNER/invoiceADMINISTRATION, OWNER/reopenADMIN, OWNER/force-statusOWNER only
Note Importanti

⚠️ INVOICED è uno stato finale PERMANENTE - neanche OWNER può fare rollback
✅ OWNER può forzare qualsiasi altra transizione
✅ Permissive mode consente edge cases (es: SCHEDULED → CLOSED)

📁 FILE DA MODIFICARE/CREARE
1. ENUMERAZIONI
   ✅ src/main/java/marcomanfrin/atixbackend/enums/WorkStatus.java
   Azione: AGGIORNARE (già esiste ma non è utilizzato)
   Aggiungere lo stato INVOICED all'enum esistente:
   javapublic enum WorkStatus {
   SCHEDULED,      // Esistente
   IN_PROGRESS,    // Esistente  
   COMPLETED,      // Rinominare in CLOSED
   INVOICED        // NUOVO - aggiungere
   }
   Oppure sostituire completamente con:
   → Vedi file: backend-changes/WorkStatus.java

2. ECCEZIONI
   ✅ src/main/java/marcomanfrin/atixbackend/exception/InvalidWorkflowTransitionException.java
   Azione: CREARE NUOVO FILE
   → Vedi file: backend-changes/InvalidWorkflowTransitionException.java

3. SERVICE LAYER
   ✅ src/main/java/marcomanfrin/atixbackend/service/WorkStateMachine.java
   Azione: CREARE NUOVO FILE
   State machine con permissive mode:

Valida transizioni di stato
OWNER può forzare quasi tutto (tranne da INVOICED)
Permette rollback e edge cases

→ Vedi file: backend-changes/WorkStateMachine.java

4. ENTITÀ
   ✅ src/main/java/marcomanfrin/atixbackend/entity/Work.java
   Azione: MODIFICARE ENTITÀ ESISTENTE
   Campi da aggiungere:
   java@Enumerated(EnumType.STRING)
   @Column(nullable = false)
   @Builder.Default
   private WorkStatus status = WorkStatus.SCHEDULED;

private LocalDateTime statusChangedAt;

@Column(name = "status_changed_by")
private UUID statusChangedBy;
Campi da deprecare (NON RIMUOVERE):
java@Deprecated(since = "2.0", forRemoval = true)
@Column(name = "completed")
private Boolean completed;

@Deprecated(since = "2.0", forRemoval = true)
@Column(name = "completed_at")
private LocalDateTime completedAt;

@Deprecated(since = "2.0", forRemoval = true)
@Column(name = "invoiced")
private Boolean invoiced;

@Deprecated(since = "2.0", forRemoval = true)
@Column(name = "invoiced_at")
private LocalDateTime invoicedAt;
Helper methods da aggiungere:
javapublic boolean isScheduled() { return this.status == WorkStatus.SCHEDULED; }
public boolean isInProgress() { return this.status == WorkStatus.IN_PROGRESS; }
public boolean isClosed() { return this.status == WorkStatus.CLOSED; }
public boolean isInvoiced() { return this.status == WorkStatus.INVOICED; }
public boolean isCompleted() {
return this.status == WorkStatus.CLOSED || this.status == WorkStatus.INVOICED;
}
→ Vedi file completo: backend-changes/Work.java

✅ src/main/java/marcomanfrin/atixbackend/entity/Ticket.java
Azione: MODIFICARE ENTITÀ ESISTENTE
Campi da aggiungere:
javaprivate LocalDateTime statusChangedAt;

@Column(name = "status_changed_by")
private UUID statusChangedBy;
Helper methods da aggiungere:
javapublic boolean isOpen() { return this.status == TicketStatus.OPEN; }
public boolean isInProgress() { return this.status == TicketStatus.IN_PROGRESS; }
public boolean isResolved() { return this.status == TicketStatus.RESOLVED; }
public boolean isClosed() { return this.status == TicketStatus.CLOSED; }
→ Vedi file completo: backend-changes/Ticket.java

5. SERVICE
   ✅ src/main/java/marcomanfrin/atixbackend/service/WorkService.java
   Azione: MODIFICARE SERVICE ESISTENTE
   Dipendenze da aggiungere:
   javaprivate final WorkStateMachine stateMachine;
   Metodi da aggiungere:
   javapublic void startWork(UUID workId, UUID technicianId, UserRole userRole)
   public void reopenWork(UUID workId, UUID userId, UserRole userRole)
   public void forceStatusChange(UUID workId, WorkStatus newStatus, UUID ownerId, UserRole userRole)
   private void syncTicketStatus(Ticket ticket, TicketStatus newStatus, UUID changedBy)
   Metodi da modificare:
   javapublic WorkDetailDTO createWork(...)
   // Aggiungere:
   // - status(WorkStatus.SCHEDULED)
   // - statusChangedAt(LocalDateTime.now())
   // - statusChangedBy(currentUserId)

public void closeWork(...)
// Modificare per usare status invece di completed

public void invoiceWork(...)
// Modificare per usare status invece di invoiced
→ Vedi file completo: backend-changes/WorkService.java

6. CONTROLLER
   ✅ src/main/java/marcomanfrin/atixbackend/controller/WorksController.java
   Azione: MODIFICARE CONTROLLER ESISTENTE
   Nuovi endpoint da aggiungere:
   java@PatchMapping("/{id}/start")
   @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'USER')")
   public ResponseEntity<Void> startWork(@PathVariable UUID id, ...)

@PatchMapping("/{id}/reopen")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
public ResponseEntity<Void> reopenWork(@PathVariable UUID id, ...)

@PatchMapping("/{id}/force-status")
@PreAuthorize("hasRole('OWNER')")
public ResponseEntity<Void> forceStatusChange(
@PathVariable UUID id,
@RequestParam WorkStatus newStatus,
...
)
Endpoint da modificare:
java// closeWork() e invoiceWork() già esistono
// Vanno solo adeguati per chiamare i nuovi metodi del service
→ Vedi file completo: backend-changes/WorksController.java

7. SPECIFICATION
   ✅ src/main/java/marcomanfrin/atixbackend/specification/WorkSpecification.java
   Azione: MODIFICARE SPECIFICATION ESISTENTE
   Aggiunger filtri per status:
   java// Filtro singolo status
   if (filters.getStatus() != null) {
   predicates.add(cb.equal(root.get("status"), filters.getStatus()));
   }

// Filtri multipli status
if (filters.getStatuses() != null && !filters.getStatuses().isEmpty()) {
predicates.add(root.get("status").in(filters.getStatuses()));
}
Aggiornare filtri deprecati:
javaif (filters.getCompleted() != null) {
if (filters.getCompleted()) {
predicates.add(root.get("status").in(WorkStatus.CLOSED, WorkStatus.INVOICED));
} else {
predicates.add(root.get("status").in(WorkStatus.SCHEDULED, WorkStatus.IN_PROGRESS));
}
}
→ Vedi file completo: backend-changes/WorkSpecification.java

8. DTO
   ✅ src/main/java/marcomanfrin/atixbackend/dto/work/WorkFilterDTO.java
   Azione: MODIFICARE DTO ESISTENTE
   Campi da aggiungere:
   javaprivate WorkStatus status;
   private List<WorkStatus> statuses;
   Campi da deprecare (NON RIMUOVERE):
   java@Deprecated(since = "2.0")
   private Boolean completed;

@Deprecated(since = "2.0")
private Boolean invoiced;
→ Vedi file completo: backend-changes/WorkFilterDTO.java

🔧 PASSI DI IMPLEMENTAZIONE
Fase 1: Setup Iniziale

✅ Creare InvalidWorkflowTransitionException.java
✅ Aggiornare WorkStatus.java (aggiungere INVOICED)
✅ Creare WorkStateMachine.java

Fase 2: Entità

✅ Modificare Work.java (aggiungere campi status)
✅ Modificare Ticket.java (aggiungere campi tracking)
✅ Eseguire migration SQL

Fase 3: Service Layer

✅ Modificare WorkService.java (nuovi metodi workflow)
✅ Testare logica di transizione stati
✅ Testare sincronizzazione ticket

Fase 4: Controller e DTO

✅ Modificare WorksController.java (nuovi endpoint)
✅ Modificare WorkFilterDTO.java (nuovi filtri)
✅ Modificare WorkSpecification.java (filtri status)