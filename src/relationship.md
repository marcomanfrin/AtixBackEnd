Riepilogo Relazioni tra Entità

1:1 (UNO A UNO)

| Entità A | Relazione | Entità B   | Note                                               |
  |----------|-----------|------------|----------------------------------------------------|
| Work     | 1:1       | Ticket     | Un lavoro può avere al massimo un ticket associato |
| Work     | 1:1       | WorkReport | Ogni lavoro ha un solo rapporto di lavoro          |

  ---
N:1 (MOLTI A UNO)

| Entità (N)                  | Relazione | Entità (1)           | Note                                           |
  |-----------------------------|-----------|----------------------|------------------------------------------------|
| Work                        | N:1       | Plant                | Più lavori appartengono allo stesso impianto   |
| Work                        | N:1       | SellerUser           | Un venditore gestisce più lavori               |
| Work                        | N:1       | Client (atixClient)  | Cliente Atix associato a più lavori            |
| Work                        | N:1       | Client (finalClient) | Cliente finale associato a più lavori          |
| WorkAssignment              | N:1       | Work                 | Più assegnazioni per lo stesso lavoro          |
| WorkAssignment              | N:1       | User                 | Un utente può avere più assegnazioni           |
| WorksiteReferenceAssignment | N:1       | Work                 | Più referenti per lo stesso lavoro             |
| WorksiteReferenceAssignment | N:1       | WorksiteReference    | Un referente può essere assegnato a più lavori |
| WorkReportEntry             | N:1       | WorkReport           | Più entry appartengono allo stesso report      |
| AttachmentLink              | N:1       | Attachment           | Più link puntano allo stesso allegato          |

  ---
N:M (MOLTI A MOLTI)

| Entità A   | Junction Table              | Entità B          | Constraint                                    |
  |------------|-----------------------------|-------------------|-----------------------------------------------|
| Work       | WorkAssignment              | User              | UNIQUE(work_id, user_id)                      |
| Work       | WorksiteReferenceAssignment | WorksiteReference | UNIQUE(work_id, worksite_reference_id)        |
| Attachment | AttachmentLink              | Entità Generiche  | UNIQUE(attachment_id, target_type, target_id) |

  ---
DETTAGLI PARTICOLARI

🔹 Client - Doppio Ruolo

Client ─┬─→ Work (come atixClient)    [N:1]
└─→ Work (come finalClient)    [N:1]
Lo stesso client può comparire in entrambi i ruoli su lavori diversi (o sullo stesso).

🔹 AttachmentLink - Polimorfismo

AttachmentLink:
- targetType: enum (WORK, TICKET, etc.)
- targetId: UUID generico
Permette di collegare allegati a qualsiasi entità senza FK tradizionali.

🔹 User - Inheritance

User (abstract)
├─→ TechnicianUser
├─→ SellerUser
└─→ AdministrativeUser

Strategy: SINGLE_TABLE

  ---
CONTEGGIO FINALE

- 1:1: 2 relazioni
- N:1: 10 relazioni
- N:M: 3 relazioni (con junction tables)
- Totale: 15 relazioni mappate
