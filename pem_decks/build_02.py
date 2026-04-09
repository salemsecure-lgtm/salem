#!/usr/bin/env python3
"""Build Deck 2: Pediatric Airway Emergencies and Difficult Airway"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 2,
    'short_title': 'Airway Emergencies',
    'title': 'Pediatric Airway Emergencies\nand the Difficult Airway',
    'subtitle': 'Anatomy, Assessment, RSI, Rescue Strategies, and Post-Intubation Management',
    'objectives': [
        'Describe the key anatomic differences of the pediatric airway',
        'Select appropriate equipment by age using standardized sizing',
        'Recognize impending airway failure and indications for intervention',
        'Execute a pediatric RSI sequence with correct drug dosing',
        'Implement a structured difficult airway algorithm for children',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Airway Anatomy — Why Children Are Different',
         'bullets': [
             'Proportionally large occiput → sniffing position with shoulder roll in infants',
             'Large tongue relative to oral cavity → easy obstruction, difficult visualization',
             'Omega-shaped, floppy epiglottis → use Miller (straight) blade in infants',
             'Larynx is higher (C3-C4 in infant vs C5-C6 in adult) and more anterior',
             'Cricoid ring is the narrowest point (< 8 years) — subglottic stenosis risk',
             'Shorter trachea (4-5 cm in neonate) → high risk of right mainstem intubation',
             '>Functional residual capacity is LOW → desaturation occurs rapidly during apnea',
         ],
         'notes': 'These anatomic differences drive everything about pediatric airway management. The large occiput means neutral/"sniffing" position requires a shoulder roll in infants. The anterior, high larynx is why straight blades work better in young children — you lift the epiglottis directly. The small FRC means you have seconds, not minutes, of apnea tolerance.'},

        {'type': 'table', 'title': 'Age-Based Equipment Sizing',
         'headers': ['Age/Weight', 'ETT Size (cuffed)', 'ETT Depth (lip)', 'Blade', 'LMA Size', 'Suction'],
         'rows': [
             ['Preterm', '3.0 uncuffed', '6-7 cm', 'Miller 0', '1', '6-8 Fr'],
             ['Term neonate', '3.0-3.5', '8-9 cm', 'Miller 0-1', '1', '8 Fr'],
             ['6 months', '3.5', '10 cm', 'Miller 1', '1.5', '8 Fr'],
             ['1 year', '3.5-4.0', '11 cm', 'Miller 1', '1.5', '8-10 Fr'],
             ['2 years', '4.0', '12 cm', 'Miller/Mac 2', '2', '10 Fr'],
             ['4 years', '4.5', '14 cm', 'Mac 2', '2', '10 Fr'],
             ['6 years', '5.0', '16 cm', 'Mac 2', '2.5', '10-12 Fr'],
             ['8 years', '5.5', '18 cm', 'Mac 2-3', '2.5', '12 Fr'],
             ['10 years', '6.0', '19 cm', 'Mac 3', '3', '12 Fr'],
             ['12+ years', '6.5-7.0', '20-22 cm', 'Mac 3-4', '3-4', '14 Fr'],
         ],
         'notes': 'Cuffed ETT formula: (age/4) + 3.5 for cuffed tubes. Depth at lip: (age/2) + 12 for children >1 year, or ETT size × 3. Always have one size up and one size down ready. Cuffed tubes are now preferred even in young children per current evidence — just keep cuff pressure <20-25 cmH2O.'},

        {'type': 'content', 'title': 'Recognizing Impending Airway Failure',
         'bullets': [
             'Progressive stridor — especially biphasic (indicates critical narrowing)',
             'Diminishing air movement with increasing effort ("getting tired")',
             'Tripod positioning, drooling, refusal to lie down',
             'Altered mental status — the child who stops fighting is in trouble',
             'SpO2 falling below 90% despite supplemental O2',
             'Bradycardia — the child\'s last compensatory mechanism has failed',
             '>The quiet child with retractions is more dangerous than the screaming child',
         ],
         'notes': 'Impending airway failure is a clinical diagnosis, not a lab value. The key red flag is a child whose work of breathing is increasing while their air movement is decreasing — they are tiring out. A sudden change from agitated to quiet in a child with respiratory distress is ominous. Do not wait for the SpO2 to drop to intervene.'},

        {'type': 'algorithm', 'title': 'Pre-Intubation Optimization: The "7 Ps"',
         'steps': [
             'Preparation: equipment, team, medications, plan B',
             'Preoxygenation: 3 min NRB or HFNC; apneic oxygenation with NC at 1L/kg/min',
             'Patient positioning: sniffing/ramped position; shoulder roll for infants',
             'Pretreatment: atropine 0.02 mg/kg if <1 year or giving succinylcholine',
             'Paralysis + Induction: chosen agents (see RSI drugs)',
             'Placement: direct or video laryngoscopy; confirm with EtCO2',
             'Post-intubation: secure tube, CXR, ventilator settings, sedation',
         ],
         'notes': 'The 7 Ps provide a systematic checklist. Preoxygenation is critical — 3 minutes of tidal breathing on NRB gives you an oxygen reservoir for apnea. Apneic oxygenation via nasal cannula during laryngoscopy is now standard practice. Atropine is recommended for infants to prevent vagal bradycardia during intubation.'},

        {'type': 'table', 'title': 'RSI Drug Selection and Dosing',
         'headers': ['Drug', 'Class', 'Dose', 'Onset', 'Key Notes'],
         'rows': [
             ['Etomidate', 'Induction', '0.3 mg/kg IV', '30-60 sec', 'Hemodynamically stable; avoid in sepsis (adrenal)'],
             ['Ketamine', 'Induction', '1-2 mg/kg IV', '60 sec', 'Bronchodilator; maintains hemodynamics; ↑ICP is a myth'],
             ['Propofol', 'Induction', '1-3 mg/kg IV', '30 sec', 'Causes hypotension; avoid in shock'],
             ['Midazolam', 'Induction', '0.1-0.3 mg/kg IV', '60-90 sec', 'Unreliable; significant hypotension'],
             ['Rocuronium', 'Paralytic', '1-1.2 mg/kg IV', '60 sec', 'Preferred paralytic; reversible with sugammadex'],
             ['Succinylcholine', 'Paralytic', '2 mg/kg IV (child)', '45 sec', 'Short-acting; avoid in hyperK, burns, crush, myopathy'],
         ],
         'notes': 'Ketamine is the go-to induction agent in PEM — it maintains BP, provides bronchodilation, and has a wide safety margin. The old concern about ICP elevation has been debunked. Rocuronium at 1.2 mg/kg provides intubating conditions equivalent to succinylcholine with the benefit of sugammadex reversibility.'},

        {'type': 'algorithm', 'title': 'Pediatric Difficult Airway Algorithm',
         'steps': [
             'Attempt 1: Direct or video laryngoscopy with optimal positioning',
             'Attempt 2: Adjust — reposition, change blade, use bougie, BURP/ELM',
             'Attempt 3: Switch provider or switch to video laryngoscopy if not used',
             'FAILED — Cannot intubate: place supraglottic airway (LMA)',
             'FAILED LMA — Cannot ventilate: BVM with two-person technique + OPA/NPA',
             'CANNOT OXYGENATE: Needle cricothyrotomy (< 8-10y) or surgical cric (>10-12y)',
         ],
         'notes': 'The key message: limit to 3 laryngoscopy attempts before moving to plan B. Each attempt causes edema and bleeding, making subsequent attempts harder. The LMA is your best rescue device. Needle cricothyrotomy in young children uses a 14-gauge angiocatheter through the cricothyroid membrane, attached to a 3.0 ETT adapter for BVM ventilation. Surgical cricothyrotomy is only for children over 10-12 years.'},

        {'type': 'content', 'title': 'Video Laryngoscopy in Pediatric Airway',
         'bullets': [
             'Now considered first-line in many PEM centers for all intubations',
             'Improves glottic visualization by 1-2 Cormack-Lehane grades',
             'Allows team visualization — better teaching and supervision',
             'Available sizes for neonates through adolescents',
             'Hyperangulated blades: better view, but harder tube delivery — use stylet',
             'Standard geometry blades (C-MAC): can also be used as direct laryngoscope',
             '>Does NOT replace the need to learn direct laryngoscopy fundamentals',
         ],
         'notes': 'Video laryngoscopy has transformed pediatric airway management. First-pass success rates are higher with VL in most studies. However, fellows must still master direct laryngoscopy because VL may not always be available. The hyperangulated blade gives a spectacular view but requires a pre-shaped stylet to deliver the tube around the angle.'},

        {'type': 'content', 'title': 'Foreign Body Airway Obstruction (FBAO)',
         'bullets': [
             'Peak age: 6 months to 3 years — coins, toys, nuts, hot dogs, grapes',
             'CONSCIOUS + mild obstruction: encourage coughing, do NOT intervene',
             'CONSCIOUS + severe obstruction (silent cough, cyanosis):',
             '>< 1 year: 5 back blows + 5 chest thrusts (NO abdominal thrusts)',
             '>> 1 year: abdominal thrusts (Heimlich maneuver)',
             'UNCONSCIOUS: begin CPR; look in mouth before each breath — remove visible FB',
             'Persistent: direct laryngoscopy + Magill forceps in ED',
             '>Do NOT perform blind finger sweeps in infants or children',
         ],
         'notes': 'FBAO is a true airway emergency where seconds matter. The key distinction is mild vs severe obstruction. If the child is coughing effectively, do not intervene — you may convert partial to complete obstruction. For complete obstruction, follow age-based BLS guidelines. In the ED, if basic maneuvers fail, use direct laryngoscopy to visualize and remove the foreign body with Magill forceps.'},

        {'type': 'content', 'title': 'Post-Intubation Management',
         'bullets': [
             'CONFIRM placement: EtCO2 waveform (gold standard), bilateral breath sounds, CXR',
             'Secure the tube: commercial holder or tape; note depth at lip',
             'Ventilator settings: Tidal volume 6-8 mL/kg; RR age-appropriate; PEEP 5-8',
             'Sedation: midazolam 0.1 mg/kg/hr + fentanyl 1-2 mcg/kg/hr infusions',
             'Monitor: continuous EtCO2, SpO2, HR, BP; ABG within 30 min',
             'Prevent: accidental extubation (biggest risk), right mainstem migration',
             '>Reassess tube position with every patient movement or transport',
         ],
         'notes': 'Post-intubation care is where many complications occur. Continuous EtCO2 is mandatory — it is the earliest indicator of tube dislodgement. The most common complication is right mainstem intubation, especially in small children where the margin of error is millimeters. Always secure the tube well and document the depth at the lip.'},

        {'type': 'case', 'title': '8-Month-Old with Stridor and Desaturation',
         'scenario': 'An 8-month-old boy is brought in with 2 days of cough, runny nose, and low-grade fever. Tonight, parents noticed a "barky cough" and noisy breathing. On arrival: audible inspiratory stridor at rest, moderate retractions, SpO2 88% on room air, HR 175, RR 55. Given blow-by O2 — SpO2 improves to 92%. Child appears tired and has decreased air entry bilaterally.',
         'questions': [
             'What is your PAT assessment? (Abnormal appearance, abnormal breathing, normal circulation — respiratory failure)',
             'Initial management? (Nebulized epinephrine 0.5 mL/kg of 1:1000, max 5 mL + dexamethasone 0.6 mg/kg)',
             'No improvement after 2 doses of racemic epi — what are you considering beyond croup?',
             'Preparation if intubation needed: what ETT size? (3.5 cuffed, have 3.0 and 4.0 ready)',
         ],
         'notes': 'This case teaches that croup can progress to respiratory failure. The stridor at rest with desaturation indicates severe airway obstruction. If standard croup management fails, consider bacterial tracheitis, foreign body, or congenital airway lesion. If intubation is needed, use a tube 0.5-1.0 sizes smaller than expected due to subglottic edema.'},

        {'type': 'pitfalls', 'title': 'Airway Pitfalls in PEM',
         'items': [
             'Failing to preoxygenate adequately — children desaturate in seconds, not minutes',
             'Underdosing paralytic: rocuronium 0.6 mg/kg is inadequate — use 1-1.2 mg/kg for RSI',
             'Not having a plan B BEFORE attempt 1 — always prepare LMA and cric kit',
             'Forcing supine position in a child maintaining their own airway with stridor',
             'Forgetting atropine in infants — vagal bradycardia during intubation can be fatal',
             'Right mainstem intubation — verify depth with formula and CXR every time',
             'Not using EtCO2 for continuous confirmation after intubation',
         ],
         'notes': 'These are the errors that harm patients. The paralytic dosing error is extremely common — fellows often use adult dosing (0.6 mg/kg rocuronium) which is insufficient for RSI conditions. The ETT depth error is preventable with the formula and a CXR. EtCO2 is mandatory — it is the earliest warning of tube dislodgement.'},
    ],
    'takeaways': [
        'Pediatric airway anatomy is fundamentally different — plan and size accordingly',
        'Preoxygenate aggressively; children have minimal apnea reserve',
        'Ketamine + rocuronium (1.2 mg/kg) is the PEM RSI combination of choice',
        'Limit to 3 intubation attempts → LMA rescue → surgical airway if cannot oxygenate',
        'Continuous EtCO2 after intubation is non-negotiable — it detects dislodgement first',
    ],
    'references': [
        'Nishisaki A, et al. Effect of just-in-time simulation training on tracheal intubation procedure safety. Pediatrics. 2010;125(6):e1292-e1299.',
        'Black AE, Flynn PE, et al. Development of a guideline for the management of the unanticipated difficult airway in pediatric practice. Paediatr Anaesth. 2015;25(4):346-362.',
        'Topjian AA, Raymond TT, et al. Pediatric Basic and Advanced Life Support: 2020 AHA Guidelines. Circulation. 2020;142(16_suppl_2):S469-S523.',
        'Weiss M, et al. Cuffed vs uncuffed tracheal tubes in children: a randomised controlled trial. Lancet. 2009;374(9695):1102-1107.',
        'Nagler J, Bachur RG. Advanced airway management. Curr Opin Pediatr. 2009;21(3):299-305.',
        'Szmuk P, et al. A comparison of GlideScope video laryngoscopy and direct laryngoscopy in children. Paediatr Anaesth. 2012;22(11):1092-1097.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '02_airway_emergencies.pptx'))
