#!/usr/bin/env python3
"""Build Deck 6: Status Asthmaticus"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 6,
    'short_title': 'Status Asthmaticus',
    'title': 'Status Asthmaticus',
    'subtitle': 'Severity Assessment, Stepwise Escalation, and ICU-Level Management in the ED',
    'objectives': [
        'Classify asthma severity using the Pediatric Asthma Score (PAS)',
        'Execute stepwise management: albuterol, ipratropium, steroids, magnesium',
        'Know when and how to escalate: IV terbutaline, ketamine, NIV',
        'Understand the risks of intubation in severe asthma',
        'Identify complications: pneumothorax, pneumomediastinum',
    ],
    'slides': [
        {'type': 'content', 'title': 'Status Asthmaticus: Defining the Emergency',
         'bullets': [
             'Definition: severe asthma exacerbation that does not respond to standard bronchodilator therapy',
             'Asthma affects ~6 million US children; ED visits >1.7 million/year',
             'Asthma deaths in children: ~150/year in the US — most are preventable',
             'Risk factors for fatal asthma: prior ICU admission, prior intubation, recent ED visit, poor compliance',
             '>The child who has been intubated before for asthma is your highest-risk patient',
         ],
         'notes': 'Frame status asthmaticus as a true emergency. The children who die from asthma are often those with known severe disease who present late or have poor medication compliance. Ask about prior intubations — this single history item identifies your highest-risk patients.'},

        {'type': 'table', 'title': 'Pediatric Asthma Score (PAS)',
         'headers': ['Component', 'Score 1 (Mild)', 'Score 2 (Moderate)', 'Score 3 (Severe)'],
         'rows': [
             ['Respiratory Rate', 'Normal for age', 'Elevated for age', '>50 (2-5y), >40 (6-12y), >30 (>12y)'],
             ['O2 Saturation', '>95%', '90-95%', '<90%'],
             ['Auscultation', 'End-expiratory wheeze only', 'Expiratory wheeze throughout', 'Inspiratory & expiratory wheeze / diminished'],
             ['Retractions', 'None or mild intercostal', 'Intercostal + substernal', 'Severe with nasal flaring'],
             ['Dyspnea', 'Speaks in sentences', 'Speaks in phrases', 'Speaks in words / unable to speak'],
         ],
         'notes': 'PAS 5-7 = mild, 8-11 = moderate, 12-15 = severe. This score helps standardize assessment and communication. Note that a "quiet chest" with minimal wheezing and severe retractions is worse than loud wheezing — it means air movement is critically reduced. Always score before AND after treatments to track response.'},

        {'type': 'algorithm', 'title': 'Stepwise Management of Status Asthmaticus',
         'steps': [
             'Step 1: Continuous albuterol 0.5 mg/kg/hr (max 20 mg/hr) + ipratropium 0.5 mg q20min x3',
             'Step 2: Systemic steroids — dexamethasone 0.6 mg/kg (max 16 mg) OR methylpred 2 mg/kg',
             'Step 3: Magnesium sulfate 50 mg/kg IV over 20 min (max 2g)',
             'Step 4: IV terbutaline 10 mcg/kg load → 0.2-0.4 mcg/kg/min infusion',
             'Step 5: Ketamine 0.5-2 mg/kg IV bolus → 0.5-2 mg/kg/hr infusion',
             'Step 6: NIV (BiPAP) or heliox if available',
             'Step 7: Intubation — LAST RESORT (most dangerous intubation in PEM)',
         ],
         'notes': 'This is your roadmap. Steps 1-3 should happen rapidly in the first 30-60 minutes for severe presentations. Magnesium is underused — give it early in severe asthma. Terbutaline requires ICU-level monitoring. Ketamine is an excellent bronchodilator and provides dissociative sedation for NIV. Intubation is truly last resort.'},

        {'type': 'table', 'title': 'Key Drug Dosing in Status Asthmaticus',
         'headers': ['Drug', 'Route', 'Dose', 'Frequency', 'Notes'],
         'rows': [
             ['Albuterol', 'Neb continuous', '0.5 mg/kg/hr (max 20 mg/hr)', 'Continuous', 'Monitor HR, K+, tremor'],
             ['Ipratropium', 'Neb', '0.5 mg (250 mcg if <5y)', 'q20min x 3 doses', 'Only useful in first hour'],
             ['Dexamethasone', 'PO/IV', '0.6 mg/kg (max 16 mg)', 'Once or x2 days', 'Equal to 5-day pred course'],
             ['Methylprednisolone', 'IV', '2 mg/kg load, then 1 mg/kg q6h', 'q6h', 'For admitted/severe'],
             ['Magnesium sulfate', 'IV', '50 mg/kg (max 2g)', 'Over 20 min', 'Monitor BP, hypotension risk'],
             ['Terbutaline', 'IV', '10 mcg/kg load → 0.2-0.4 mcg/kg/min', 'Continuous', 'Cardiac monitoring; check troponin'],
             ['Ketamine', 'IV', '0.5-2 mg/kg bolus, 0.5-2 mg/kg/hr', 'Continuous', 'Bronchodilator + sedation'],
             ['Epinephrine', 'IM', '0.01 mg/kg (1:1000, max 0.3 mg)', 'q15-20 min', 'Bridge while setting up nebs'],
         ],
         'notes': 'Dexamethasone is now preferred over prednisone/prednisolone for most asthma exacerbations — better compliance, less vomiting, 1-2 doses equivalent to 5-day course. Magnesium causes vasodilation so monitor BP during infusion. Terbutaline requires continuous cardiac monitoring and can cause troponin elevation and lactic acidosis — these are expected, not reasons to stop.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'The silent chest is the most dangerous chest.',
         'sub': 'A child with severe asthma who has NO wheezing on auscultation has critically reduced air movement. This is worse than loud wheezing. Do not be falsely reassured by the absence of wheezing in a child with respiratory distress.',
         'notes': 'This pearl saves lives. Fellows sometimes interpret improvement in wheezing as clinical improvement. If wheezing decreases but the child is still in distress with retractions and poor air entry, they are getting WORSE, not better. The silent chest means air is not moving.'},

        {'type': 'content', 'title': 'Intubation in Asthma: The Most Dangerous Intubation',
         'bullets': [
             'Intubation in status asthmaticus carries high mortality — avoid if at all possible',
             'Reasons: severe dynamic hyperinflation, air trapping, high airway pressures',
             'Post-intubation: risk of hemodynamic collapse from auto-PEEP and decreased venous return',
             'If must intubate: ketamine induction (bronchodilator), rocuronium paralysis',
             'Ventilator strategy: low RR (10-14), long expiratory time (I:E 1:4-1:5)',
             'Permissive hypercapnia: accept pCO2 60-80; target pH >7.20',
             'Disconnect and manually decompress if sudden deterioration (DOPE: Displacement, Obstruction, Pneumothorax, Equipment)',
             '>Have a chest tube tray at bedside — pneumothorax risk is high',
         ],
         'notes': 'This slide should make your audience appropriately fearful of intubating asthmatics. The physiology of positive pressure ventilation with severe air trapping is deadly — increased intrathoracic pressure reduces venous return and can cause cardiovascular collapse. If the patient suddenly deteriorates after intubation, disconnect from the vent and allow full exhalation, then consider DOPE mnemonic.'},

        {'type': 'two_column', 'title': 'Treatment Working vs Time to Escalate',
         'left_title': 'IMPROVING (Continue Current Rx)',
         'left': [
             'PAS score decreasing',
             'Work of breathing improving',
             'Air entry increasing',
             'Wheezing present but with good air movement',
             'SpO2 improving or stable ≥94%',
             'Child able to speak, more interactive',
             'Can wean continuous albuterol to intermittent',
         ],
         'right_title': 'NOT IMPROVING (Escalate NOW)',
         'right': [
             'PAS score unchanged or increasing after 1 hour',
             'Silent chest or decreasing air movement',
             'Altered mental status or exhaustion',
             'SpO2 declining despite treatment',
             'Rising CO2 on VBG (respiratory acidosis)',
             'Unable to tolerate nebulizer',
             'Requiring FiO2 >0.5 to maintain SpO2',
         ],
         'notes': 'This decision framework helps at the 60-minute mark. A child who has received continuous albuterol, ipratropium x3, steroids, and magnesium and is still in severe distress needs IV terbutaline or ketamine. Do not keep repeating the same treatments hoping for improvement. Escalate early and consult PICU.'},

        {'type': 'case', 'title': '7-Year-Old Not Responding to Nebulizers',
         'scenario': 'A 7-year-old boy (28 kg) with known severe persistent asthma presents with acute exacerbation x6 hours. Already received 2 albuterol nebs from EMS. On arrival: sitting upright, speaking in single words, audible wheezing, severe intercostal and subcostal retractions, SpO2 89% on room air. HR 155, RR 38. PAS score: 14/15. Started on continuous albuterol + ipratropium + IV dexamethasone.',
         'questions': [
             'After 30 minutes: PAS still 13, SpO2 92% on 6L NC — next steps?',
             'Magnesium sulfate: 50 mg/kg = 1400 mg IV over 20 minutes',
             'After magnesium, still PAS 12 — now what? (Start IV terbutaline, call PICU)',
             'The child becomes somnolent — what are you worried about? (Respiratory failure, fatigue, rising CO2)',
         ],
         'notes': 'Walk through this case in real-time. The key teaching: a PAS of 14 means this child is in extremis. Standard therapy is unlikely to be enough. Give magnesium early, start terbutaline early, and call PICU early. The somnolence at the end is the most critical red flag — the child is tiring out and heading toward respiratory failure.'},

        {'type': 'pitfalls', 'title': 'Status Asthmaticus Pitfalls',
         'items': [
             'Delayed systemic steroids — give within 15 minutes of recognition of severe asthma',
             'Inadequate albuterol dosing — intermittent nebs are insufficient for severe exacerbation',
             'Premature intubation — exhaust all medical options first; intubation itself is dangerous',
             'Not giving magnesium early — it is safe and effective; give at step 3, not as last resort',
             'Missing pneumothorax or pneumomediastinum — get CXR if sudden deterioration',
             'Discharging too early — must demonstrate sustained improvement off continuous nebs',
             'Not addressing trigger: was this actually anaphylaxis? Foreign body? Cardiac wheeze?',
         ],
         'notes': 'Steroid delay is the most common and most impactful error. Steroids take 4-6 hours for full effect — the earlier you give them, the sooner they work. Inadequate albuterol dosing is also very common — continuous nebulization at 0.5 mg/kg/hr is standard for severe asthma. And always reconsider the diagnosis if asthma treatment is not working.'},
    ],
    'takeaways': [
        'Use the Pediatric Asthma Score to guide treatment and track response objectively',
        'Give steroids (dexamethasone 0.6 mg/kg) within 15 minutes of recognizing severe asthma',
        'Magnesium sulfate (50 mg/kg IV) should be given early in severe exacerbations, not as a last resort',
        'Intubation in status asthmaticus is the MOST DANGEROUS intubation — exhaust all alternatives first',
        'The silent chest = critical: it means NO air movement, not improvement',
    ],
    'references': [
        'Expert Panel Report 3: Guidelines for the Diagnosis and Management of Asthma. NIH/NHLBI, 2007.',
        'Global Initiative for Asthma (GINA). Global Strategy for Asthma Management and Prevention, 2023.',
        'Keeney GE, et al. Dexamethasone for acute asthma exacerbations in children: a meta-analysis. Pediatrics. 2014;133(3):493-499.',
        'Griffiths B, Kew KM. Intravenous magnesium sulfate for treating children with acute asthma in the emergency department. Cochrane Database Syst Rev. 2016;4:CD011050.',
        'Carroll CL, Zucker AR. The increased cost of complications in children with status asthmaticus. Pediatr Pulmonol. 2007;42(10):914-919.',
        'Nievas IF, Anand KJS. Severe acute asthma exacerbation in children: a stepwise approach for escalating therapy. J Pediatr Pharmacol Ther. 2013;18(2):88-104.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '06_status_asthmaticus.pptx'))
