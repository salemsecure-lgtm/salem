#!/usr/bin/env python3
"""Build Deck 7: Bronchiolitis — Evidence-Based ED Management"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 7,
    'short_title': 'Bronchiolitis',
    'title': 'Bronchiolitis:\nEvidence-Based ED Management',
    'subtitle': 'Supporting What Works, Avoiding What Doesn\'t, and Risk-Stratifying for Disposition',
    'objectives': [
        'Diagnose bronchiolitis clinically without routine testing',
        'Apply evidence-based management: suctioning, hydration, oxygen support',
        'Identify interventions that do NOT work and should be avoided',
        'Risk-stratify for admission vs safe discharge',
        'Recognize high-risk populations: young infants, apnea, prematurity',
    ],
    'slides': [
        {'type': 'content', 'title': 'Bronchiolitis: The Most Common Lower Respiratory Infection in Infants',
         'bullets': [
             'Affects children <2 years; peak incidence 2-6 months',
             'RSV causes ~70% of cases; also rhinovirus, metapneumovirus, adenovirus, parainfluenza',
             'Seasonal: November-March in Northern Hemisphere (RSV season)',
             'Accounts for ~100,000 hospitalizations/year in the US',
             '>Leading cause of hospitalization in infants <1 year',
             'Self-limited: typical course 7-10 days, peak symptoms day 3-5',
         ],
         'notes': 'Bronchiolitis fills pediatric EDs every winter. It is a clinical diagnosis — no testing needed in the typical presentation. The key teaching point is that this is a disease of small airways with mucus plugging and edema, and the management is fundamentally supportive.'},

        {'type': 'table', 'title': 'What Works vs What Doesn\'t in Bronchiolitis',
         'headers': ['Intervention', 'Evidence', 'Recommendation'],
         'rows': [
             ['Nasal suctioning', 'Improves feeding and comfort', 'YES — first-line, before feeds and sleep'],
             ['Supplemental O2', 'Maintains SpO2 ≥90%', 'YES — wean as tolerated; target ≥90%'],
             ['HFNC', 'Reduces intubation rates (Franklin 2018)', 'YES — for moderate-severe cases'],
             ['IV/NG hydration', 'Prevents dehydration when PO fails', 'YES — when unable to feed adequately'],
             ['Albuterol/salbutamol', 'No benefit in RCTs', 'NO — do not use routinely'],
             ['Systemic corticosteroids', 'No benefit; potential harm', 'NO — not indicated'],
             ['Antibiotics', 'No benefit unless secondary infection', 'NO — viral disease'],
             ['Chest physiotherapy', 'No benefit', 'NO — not recommended'],
             ['Hypertonic saline', 'May help inpatient; no ED benefit', 'NOT in ED — possibly inpatient'],
             ['Epinephrine nebs', 'Transient effect; no admission reduction', 'NOT recommended routinely'],
         ],
         'notes': 'This is the most important teaching slide for bronchiolitis. The AAP 2014 guideline and subsequent evidence are clear: bronchiolitis management is about what you DON\'T do. Avoid the temptation of a "trial of albuterol" — it doesn\'t work, it leads to unnecessary admissions, and it creates false expectations for parents.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'Bronchiolitis management is about what you DON\'T do.',
         'sub': 'Avoid routine CXR, labs, albuterol trials, steroids, and antibiotics. Focus on suctioning, hydration, oxygen support, and reassurance. Less is more.',
         'notes': 'This is the central message of evidence-based bronchiolitis management. Every unnecessary intervention adds cost, prolongs stay, and potentially causes harm. A trial of albuterol that shows a transient response often leads to admission for "reactive airway disease" when the child actually just has bronchiolitis.'},

        {'type': 'content', 'title': 'HFNC in Bronchiolitis — The Evidence',
         'bullets': [
             'Franklin et al., NEJM 2018: HFNC reduced escalation of care (treatment failure) vs standard O2',
             'Starting flow: 2 L/kg/min; FiO2 titrated to SpO2 ≥92%',
             'Provides CPAP effect (2-5 cmH2O), dead space washout, humidification',
             'Well-tolerated in infants; can improve feeding',
             'Escalation criteria if HFNC failing: rising FiO2 >0.5, persistent tachypnea, apnea',
             '>HFNC is now standard of care for moderate-severe bronchiolitis in most PEDs',
         ],
         'notes': 'The Franklin trial changed practice worldwide. HFNC is not just supplemental oxygen — it provides positive pressure support and improves ventilation efficiency. Start early in moderate-severe cases. If FiO2 needs keep rising or the child is not improving at 60-90 minutes, consider escalation to CPAP or PICU consultation.'},

        {'type': 'content', 'title': 'Apnea Risk in Young Infants',
         'bullets': [
             'Apnea occurs in 1-5% of infants hospitalized with bronchiolitis',
             'Risk factors: age <2 months, history of prematurity (<37 weeks), history of apnea of prematurity',
             'Apnea may be the PRESENTING symptom — before respiratory distress develops',
             'All infants with bronchiolitis-associated apnea require admission for monitoring',
             'Apnea typically resolves as the illness progresses',
             '>Ask about apnea/pauses in EVERY young infant with bronchiolitis',
         ],
         'notes': 'Apnea is the most dangerous complication of bronchiolitis in young infants. It can occur early in the illness, sometimes before other symptoms develop. Always ask parents about pauses in breathing, color changes, or episodes requiring stimulation. An infant <2 months with bronchiolitis warrants admission even if they look well.'},

        {'type': 'two_column', 'title': 'Admission vs Discharge Decision',
         'left_title': 'ADMIT (Cannot Safely Discharge)',
         'left': [
             'Age <4-6 weeks',
             'History of apnea with this illness',
             'Persistent SpO2 <90% on room air',
             'Unable to maintain hydration (poor feeding)',
             'Moderate-severe respiratory distress at ED discharge',
             'Significant prematurity (<32 weeks)',
             'Hemodynamically significant CHD',
             'Immunodeficiency',
         ],
         'right_title': 'DISCHARGE (With Precautions)',
         'right': [
             'Age >2-3 months without risk factors',
             'SpO2 ≥90% on room air consistently',
             'Feeding adequately (>50% of normal)',
             'Respiratory distress mild-moderate and stable/improving',
             'Reliable caregivers with return precautions',
             'Follow-up within 24-48 hours arranged',
             'Adequate home suctioning available',
             'No apnea observed during ED stay',
         ],
         'notes': 'Disposition is the hardest decision in bronchiolitis. The SpO2 threshold of 90% (not 94%) for admission reflects evidence that brief desaturations to 90% are clinically insignificant. Use the whole clinical picture — a well-feeding infant with transient desaturations to 91-92% may be safe for discharge with close follow-up.'},

        {'type': 'case', 'title': '6-Week-Old with RSV and an Apnea Episode',
         'scenario': 'A 6-week-old (ex-36 week preterm) presents with 2 days of congestion and cough. Mom reports one episode today where the baby "stopped breathing and turned blue" for ~15 seconds, then self-resolved with stimulation. On arrival: mild subcostal retractions, audible congestion, SpO2 95% on RA, HR 155, RR 48, Temp 38.2°C. Nasal aspirate positive for RSV.',
         'questions': [
             'Does this child need admission? (Absolutely — apnea + young age + prematurity = multiple risk factors)',
             'What monitoring is needed? (Continuous cardiorespiratory monitoring, pulse oximetry)',
             'Would you start albuterol? (No — no evidence of benefit in bronchiolitis)',
             'When can this child be discharged? (24-48 hours apnea-free, feeding well, SpO2 stable)',
         ],
         'notes': 'This case hits multiple high-risk features: young age, prematurity, and witnessed apnea. This child absolutely needs admission with continuous monitoring. The RSV test confirms the diagnosis but doesn\'t change management. The temptation to "do something" with albuterol should be resisted — it will not help and may falsely reassure.'},

        {'type': 'pitfalls', 'title': 'Bronchiolitis Pitfalls',
         'items': [
             'Ordering routine CXR — leads to unnecessary antibiotic prescriptions for atelectasis read as "infiltrate"',
             '"Trial of albuterol" — creates expectation, doesn\'t change outcomes, may lead to inappropriate admission',
             'Prescribing steroids — not effective and may prolong viral shedding',
             'Using SpO2 threshold of 94% instead of 90% — leads to unnecessary admissions',
             'Missing apnea in young infants — always ask about pauses and color changes',
             'Diagnosing "reactive airway disease" in a 3-month-old — this is bronchiolitis',
             'Not educating parents on natural course (7-10 days, peak day 3-5) — leads to bounce-backs',
         ],
         'notes': 'The CXR pitfall is evidence-based: routine CXR in bronchiolitis leads to antibiotic prescriptions 25% of the time for findings that are not bacterial. The SpO2 target change from 94% to 90% reflects evidence that oxygen saturation monitoring drives hospitalization without improving outcomes.'},
    ],
    'takeaways': [
        'Bronchiolitis is a clinical diagnosis — routine CXR, labs, and viral testing are unnecessary',
        'Evidence-based management: suctioning, hydration, O2 (target SpO2 ≥90%), and HFNC if needed',
        'Do NOT give albuterol, steroids, or antibiotics — they do not work',
        'Young infants (<6 weeks), premature infants, and those with apnea are highest risk — admit',
        'Educate parents: peak symptoms day 3-5, total course 7-10 days — this reduces bounce-backs',
    ],
    'references': [
        'Ralston SL, et al. Clinical Practice Guideline: The Diagnosis, Management, and Prevention of Bronchiolitis. Pediatrics. 2014;134(5):e1474-e1502.',
        'Franklin D, et al. A randomized trial of high-flow oxygen therapy in infants with bronchiolitis. N Engl J Med. 2018;378(12):1121-1131.',
        'Gadomski AM, Scribani MB. Bronchodilators for bronchiolitis. Cochrane Database Syst Rev. 2014;6:CD001266.',
        'Fernandes RM, et al. Glucocorticoids for acute viral bronchiolitis in infants and young children. Cochrane Database Syst Rev. 2013;6:CD004878.',
        'Schuh S, et al. Effect of oximetry on hospitalization in bronchiolitis: a randomized clinical trial. JAMA. 2014;312(7):712-718.',
        'Hasegawa K, et al. Trends in bronchiolitis hospitalizations in the United States, 2000-2009. Pediatrics. 2013;132(1):28-36.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '07_bronchiolitis.pptx'))
