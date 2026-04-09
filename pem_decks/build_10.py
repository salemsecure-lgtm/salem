#!/usr/bin/env python3
"""Build Deck 10: Pediatric Procedural Sedation"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 10,
    'short_title': 'Procedural Sedation',
    'title': 'Pediatric Procedural Sedation',
    'subtitle': 'Agent Selection, Risk Stratification, and Safe Sedation Protocols for the PED',
    'objectives': [
        'Risk-stratify patients for procedural sedation using ASA classification and fasting status',
        'Select the appropriate sedation agent based on procedure type, duration, and patient factors',
        'Execute safe sedation with proper monitoring, equipment, and rescue preparedness',
        'Manage adverse events: laryngospasm, apnea, emesis, and desaturation',
        'Apply evidence-based fasting guidelines and understand when they do vs. do not apply',
    ],
    'slides': [
        {'type': 'content', 'title': 'Procedural Sedation in PEM: Core Principles',
         'bullets': [
             'Procedural sedation is one of the core competencies of PEM fellowship',
             'Goal: provide anxiolysis, analgesia, and/or immobility for a procedure in a safe, controlled manner',
             'Sedation exists on a continuum: minimal → moderate → deep → general anesthesia',
             'Any sedation agent can push a patient deeper than intended — you must be prepared for the next level',
             'The person monitoring the patient should NOT be the person performing the procedure',
             '>Rule of thumb: if you cannot rescue one level deeper than your target, you should not sedate that patient',
         ],
         'notes': 'This foundational slide sets the tone. The key concept: sedation is a continuum. You aim for moderate or deep sedation, but any patient can transiently become apneic or lose airway protective reflexes. You must always be prepared to rescue from general anesthesia — meaning full airway management equipment and skills at the bedside.'},

        {'type': 'table', 'title': 'Sedation Depth Continuum',
         'headers': ['Parameter', 'Minimal (Anxiolysis)', 'Moderate', 'Deep', 'General Anesthesia'],
         'rows': [
             ['Responsiveness', 'Normal to verbal', 'Purposeful to verbal/tactile', 'Purposeful to repeated/painful', 'Unarousable'],
             ['Airway', 'Unaffected', 'No intervention needed', 'May need intervention', 'Intervention often required'],
             ['Spontaneous Ventilation', 'Unaffected', 'Adequate', 'May be inadequate', 'Frequently inadequate'],
             ['Cardiovascular', 'Unaffected', 'Usually maintained', 'Usually maintained', 'May be impaired'],
             ['Example Agent', 'Midazolam PO', 'Ketamine IM', 'Propofol IV', 'Propofol + paralytic'],
         ],
         'notes': 'This table is from the ASA continuum of sedation definition. The critical teaching: the columns blend into each other. There is no hard line between moderate and deep sedation. This is why preparation for the next level is mandatory. If you are targeting moderate sedation, you must have equipment and skills for deep sedation rescue.'},

        {'type': 'content', 'title': 'Pre-Sedation Assessment',
         'bullets': [
             'ASA Physical Status Classification:',
             '>ASA I: Normal healthy child',
             '>ASA II: Mild systemic disease (well-controlled asthma, obesity)',
             '>ASA III: Severe systemic disease (poorly controlled asthma, significant cardiac disease)',
             '>ASA IV+: Life-threatening disease — sedation by anesthesia only',
             'Airway Assessment: Mallampati score, mouth opening, neck mobility, history of difficult airway',
             'AMPLE History: Allergies, Medications, Past medical/surgical, Last meal, Events leading to procedure',
             'Targeted exam: heart, lungs, airway anatomy',
             '>ASA I-II patients are appropriate for ED sedation. ASA III = case-by-case. ASA IV = anesthesia.',
         ],
         'notes': 'Pre-sedation assessment is critical and must be documented. The ASA classification drives your risk level. Most ED sedations are ASA I-II patients and are very safe. ASA III requires careful consideration of risks and benefits and may warrant anesthesia consultation. Always assess the airway — Mallampati, mouth opening, jaw protrusion, neck extension.'},

        {'type': 'key_point', 'title': 'Clinical Pearl: Fasting Guidelines',
         'point': 'In the ED, strict NPO requirements should NOT delay emergency procedures.',
         'sub': 'AAP/ASA guidelines suggest: clear liquids 2h, breast milk 4h, solids 6-8h for ELECTIVE sedation. For EMERGENCY procedures, fasting status does not change the decision to sedate — it changes your preparation level. Evidence shows NO increased aspiration risk in ED sedation regardless of fasting status.',
         'notes': 'This is one of the most important and most debated topics in PEM sedation. The evidence is clear: in large studies of ED procedural sedation (>30,000 cases), there is no association between fasting status and adverse events including aspiration. Do not delay emergency fracture reductions or laceration repairs for fasting. Instead, ensure you have suction ready and position the patient appropriately.'},

        {'type': 'table', 'title': 'Sedation Agent Selection Guide',
         'headers': ['Agent', 'Route', 'Onset', 'Duration', 'Best For', 'Key Advantage'],
         'rows': [
             ['Ketamine', 'IV: 1-2 mg/kg', '1 min', '15-20 min', 'Fracture reduction, laceration, abscess I&D', 'Dissociative; maintains airway reflexes'],
             ['Ketamine', 'IM: 4-5 mg/kg', '5 min', '25-40 min', 'Uncooperative child, no IV access', 'No IV needed; reliable deep sedation'],
             ['Propofol', 'IV: 1 mg/kg + 0.5 mg/kg prn', '<1 min', '5-10 min', 'Short procedures, cardioversion, joint reduction', 'Ultra-short duration; rapid recovery'],
             ['Midazolam', 'IN: 0.3-0.5 mg/kg', '5-10 min', '30-60 min', 'Anxiolysis, CT scan, suturing (minor)', 'Non-invasive route; good anxiolysis'],
             ['Fentanyl + Midazolam', 'IV', '1-2 min', '20-40 min', 'Painful procedures with anxiolysis', 'Titratable; reversible (naloxone + flumazenil)'],
             ['Nitrous Oxide', 'Inhaled 50-70%', '2-3 min', 'Minutes after off', 'Laceration repair, IV starts, minor procedures', 'Patient-controlled; rapid onset/offset'],
             ['Dexmedetomidine', 'IN: 3-4 mcg/kg', '15-30 min', '45-90 min', 'MRI, echocardiography, EEG', 'No respiratory depression; natural sleep'],
         ],
         'notes': 'This is your go-to agent selection guide. Ketamine IV is the workhorse of PEM sedation — dissociative state, maintained airway reflexes, profound analgesia. Propofol is excellent for very short procedures but has NO analgesic properties. Nitrous oxide is increasingly popular for minor painful procedures and anxiolysis. Dexmedetomidine is emerging for non-painful imaging.'},

        {'type': 'content', 'title': 'Ketamine: The PEM Workhorse',
         'bullets': [
             'Dissociative agent: creates a "functional disconnection" between cortex and limbic system',
             'Unique advantage: maintains airway protective reflexes and spontaneous ventilation',
             'IV dose: 1-2 mg/kg (give slowly over 30-60 seconds to reduce emergence reactions)',
             'IM dose: 4-5 mg/kg (when IV access not available; reliable in uncooperative children)',
             'Duration: IV 15-20 min, IM 25-40 min. Redose at 0.5-1 mg/kg if needed',
             'Co-administer ondansetron 0.15 mg/kg IV (max 4 mg) to prevent vomiting',
             'Contraindications: age <3 months (relative), psychosis, conditions with increased ICP concern',
             '>Ketamine does NOT raise ICP in a clinically significant way — old dogma now debunked',
         ],
         'notes': 'Ketamine is the signature drug of PEM procedural sedation. Emphasize: it does not cause true respiratory depression (unlike propofol/opioids). The dissociative state is characterized by nystagmus, random purposeless movements, and a cataleptic stare — this is NORMAL, not a sign of under- or over-sedation. Ondansetron prophylaxis reduces vomiting from ~10-15% to ~5%.'},

        {'type': 'algorithm', 'title': 'Procedural Sedation Workflow',
         'steps': [
             'Pre-sedation: Informed consent, AMPLE history, ASA class, airway assessment, NPO status',
             'Setup: Monitoring (pulse ox, capnography, BP, HR), suction ON, BVM at bedside, airway equipment ready',
             'Personnel: Dedicated sedation nurse + provider for sedation + provider for procedure (minimum 3)',
             'Administer agent: titrate to effect; wait appropriate onset time before starting procedure',
             'Monitor: continuous pulse ox + capnography, RR, HR, BP q5min, level of sedation documented',
             'Recovery: monitor until return to baseline; assess ambulation, orientation, oral tolerance',
             'Discharge criteria: return to baseline mental status, stable vitals x30 min, tolerating PO, safe gait',
         ],
         'notes': 'This workflow should be standardized in your department. Key points: capnography detects hypoventilation BEFORE desaturation occurs — it is the earliest warning sign. Three people minimum: one to sedate, one to do the procedure, one to monitor. Never combine roles. Recovery criteria must be met before discharge.'},

        {'type': 'pitfalls', 'title': 'Sedation Adverse Events and Management',
         'items': [
             'APNEA: Most common serious event — bag-mask ventilate, reposition airway, consider naloxone if opioid',
             'LARYNGOSPASM (ketamine): Suction secretions, jaw thrust, positive pressure ventilation; if refractory → succinylcholine 0.5-1 mg/kg IV or 4 mg/kg IM',
             'EMESIS: Turn head, suction, recover airway — ondansetron prophylaxis reduces risk',
             'DESATURATION: Reposition (jaw thrust/chin lift), supplemental O2, BVM if needed; if no improvement → prepare for intubation',
             'EMERGENCE REACTION (ketamine): Reassure parents, dim lights, minimize stimulation; midazolam 0.05 mg/kg IV if severe',
             'HYPOTENSION (propofol): IV fluid bolus 20 mL/kg; reduce propofol dose in subsequent boluses',
             'PARADOXICAL AGITATION (midazolam): Consider ketamine or propofol instead; flumazenil 0.01 mg/kg if needed',
         ],
         'notes': 'Adverse event management must be rehearsed, not just known. Laryngospasm with ketamine is the most important rescue to know — it occurs in ~0.3% of cases. First-line is suctioning (secretions are the trigger) and positive pressure ventilation. If that fails, succinylcholine is the rescue drug. Emergence reactions are distressing but not dangerous.'},

        {'type': 'case', 'title': '4-Year-Old with Forearm Fracture Needing Reduction',
         'scenario': 'A 4-year-old girl (18 kg) with a displaced distal radius fracture needs closed reduction. She is otherwise healthy (ASA I), had lunch 3 hours ago, is crying and in pain. IV has been placed and she received morphine 0.1 mg/kg with partial relief. The orthopedic team is ready for reduction.',
         'questions': [
             'Best sedation agent? (Ketamine IV 1.5-2 mg/kg — provides dissociation + analgesia + immobility)',
             'Pre-medication? (Ondansetron 0.15 mg/kg IV to prevent emesis)',
             'Fasting concern? (3h since lunch — emergency procedure, do not delay for fasting)',
             'What monitoring do you need? (Pulse ox, capnography, HR, BP, dedicated sedation nurse)',
             'She vomits during recovery — management? (Turn head, suction, monitor airway, reassure)',
         ],
         'notes': 'This is the bread-and-butter PEM sedation case. Walk through every step: consent, setup, monitoring, agent selection, administration, monitoring during procedure, recovery. Ketamine is the perfect agent here: provides analgesia, immobility, and dissociation. The 3-hour fast is irrelevant for an emergency reduction. Have suction ready for emesis.'},

        {'type': 'two_column', 'title': 'Ketamine vs. Propofol: When to Use Which',
         'left_title': 'CHOOSE KETAMINE',
         'left': [
             'Painful procedure requiring analgesia (fracture, abscess)',
             'No IV access (can give IM)',
             'Need for prolonged sedation (>15 min)',
             'Hemodynamically unstable patient (ketamine supports BP)',
             'Status asthmaticus (bronchodilator effect)',
             'Uncooperative toddler without IV',
             'Concern for respiratory depression',
         ],
         'right_title': 'CHOOSE PROPOFOL',
         'right': [
             'Brief procedure (<10 min) with separate analgesia provided',
             'Cardioversion or short painful procedure with nerve block',
             'Rapid recovery desired (imaging, cardioversion)',
             'Adolescent/adult patients (emergence reactions more common)',
             'Repeated sedation needed in same visit',
             'Patient with known ketamine intolerance',
             'Procedures requiring very brief deep sedation',
         ],
         'notes': 'The key distinction: ketamine provides analgesia + sedation + immobility; propofol provides sedation + amnesia but NO analgesia. If using propofol for a painful procedure, you must provide separate analgesia (nerve block, local, opioid). Propofol has a much narrower therapeutic window and higher risk of apnea.'},
    ],
    'takeaways': [
        'Ketamine IV (1-2 mg/kg) is the workhorse agent for PEM sedation — safe, effective, maintains airway',
        'Fasting status should NOT delay emergency procedural sedation in the ED',
        'Always be prepared to rescue one level deeper than your target sedation depth',
        'Capnography is the earliest warning for hypoventilation — use it for every sedation',
        'Know your rescue protocols: laryngospasm → suction + PPV + succinylcholine if refractory',
    ],
    'references': [
        'Coté CJ, et al. Guidelines for monitoring and management of pediatric patients before, during, and after sedation for diagnostic and therapeutic procedures. Pediatrics. 2019;143(6):e20191000.',
        'Green SM, et al. Clinical practice guideline for emergency department ketamine dissociative sedation: 2011 update. Ann Emerg Med. 2011;57(5):449-461.',
        'Bellolio MF, et al. Incidence of adverse events in paediatric procedural sedation in the emergency department: a systematic review and meta-analysis. BMJ Open. 2016;6(6):e011384.',
        'Green SM, Roback MG, Krauss BS. Laryngospasm during emergency department ketamine sedation: a case-control study. Pediatr Emerg Care. 2010;26(11):798-802.',
        'Beach ML, et al. Major adverse events and relationship to nil per os status in pediatric sedation/anesthesia outside the operating room. Anesthesiology. 2016;124(1):80-88.',
        'Grunwell JR, et al. Trends and outcomes of procedural sedation outside the operating room. Pediatrics. 2020;145(5):e20193559.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '10_procedural_sedation.pptx'))
