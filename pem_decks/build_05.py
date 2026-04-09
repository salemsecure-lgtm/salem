#!/usr/bin/env python3
"""Build Deck 5: Respiratory Distress in Children — PEM Approach"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 5,
    'short_title': 'Respiratory Distress',
    'title': 'Respiratory Distress in Children:\nThe PEM Approach',
    'subtitle': 'Systematic Assessment, Localization, Oxygen Delivery, and Decision-Making',
    'objectives': [
        'Apply a systematic approach to the child in respiratory distress',
        'Localize the pathology: upper vs lower airway, parenchymal vs pleural',
        'Select appropriate oxygen delivery and respiratory support by severity',
        'Differentiate respiratory distress from respiratory failure',
        'Identify red flags requiring emergent intervention or intubation',
    ],
    'slides': [
        {'type': 'content', 'title': 'Respiratory Complaints: The #1 Reason for PED Visits',
         'bullets': [
             'Respiratory complaints account for ~30% of all pediatric ED visits',
             'Respiratory failure is the leading cause of pediatric cardiac arrest',
             'Age-specific anatomy and physiology drive disease patterns',
             '>Infants: obligate nose breathers, horizontal ribs, compliant chest wall',
             '>Toddlers: small airways, reactive airways, high metabolic rate',
             'Early recognition and intervention prevents arrest',
             'Most can be managed with oxygen, positioning, and medications',
         ],
         'notes': 'Respiratory emergencies are the bread and butter of PEM. Virtually every shift will have multiple children with breathing difficulties. The key teaching point is that respiratory failure precedes cardiac arrest in nearly all pediatric cases — intervene on the lungs and you prevent arrest.'},

        {'type': 'two_column', 'title': 'Localization: Upper vs Lower Airway',
         'left_title': 'UPPER AIRWAY (Extrathoracic)',
         'left': [
             'INSPIRATORY stridor (hallmark)',
             'Barky or hoarse cough',
             'Positional changes in symptoms',
             'Drooling, voice change',
             'Suprasternal retractions prominent',
             'Think: croup, epiglottitis, FB, abscess',
             'Worse on inspiration (dynamic collapse)',
             'Neck X-ray may be helpful',
         ],
         'right_title': 'LOWER AIRWAY (Intrathoracic)',
         'right': [
             'EXPIRATORY wheezing (hallmark)',
             'Prolonged expiratory phase',
             'Intercostal/subcostal retractions',
             'Air trapping, hyperinflation',
             'Diminished air entry if severe',
             'Think: asthma, bronchiolitis, FB, pneumonia',
             'Worse on expiration (dynamic compression)',
             'CXR: hyperinflation, infiltrates, effusion',
         ],
         'notes': 'The single most important skill is localizing the problem by auscultation and observation. Stridor = upper airway until proven otherwise. Wheeze = lower airway. Grunting suggests alveolar disease (pneumonia, pulmonary edema). A child with biphasic stridor has a fixed obstruction, which is more concerning than purely inspiratory stridor.'},

        {'type': 'table', 'title': 'Age-Based Differential Diagnosis',
         'headers': ['Age', 'Upper Airway', 'Lower Airway', 'Parenchymal'],
         'rows': [
             ['Neonate (0-28d)', 'Laryngomalacia, choanal atresia, vocal cord paralysis', 'Bronchiolitis (late)', 'RDS, TTN, pneumonia, congenital'],
             ['Infant (1-12mo)', 'Croup, subglottic stenosis, hemangioma', 'Bronchiolitis (RSV), reactive airway', 'Pneumonia, aspiration'],
             ['Toddler (1-4y)', 'Croup (peak age), FB, retropharyngeal abscess', 'Asthma, bronchiolitis (up to 2y), FB', 'Pneumonia'],
             ['School-age (5-12y)', 'Peritonsillar abscess, angioedema', 'Asthma (most common), FB (rare)', 'Pneumonia, empyema'],
             ['Adolescent (>12y)', 'Peritonsillar abscess, angioedema, anaphylaxis', 'Asthma, spontaneous pneumothorax', 'Pneumonia, PE (rare)'],
         ],
         'notes': 'This table helps you generate a differential based on age and localization. The most common diagnoses by age: neonates = bronchiolitis/pneumonia; infants = bronchiolitis; toddlers = croup/asthma; school-age = asthma; adolescents = asthma. Foreign body should always be considered in toddlers with acute-onset symptoms.'},

        {'type': 'content', 'title': 'Work of Breathing Assessment',
         'bullets': [
             'NASAL FLARING: alae nasi recruitment — indicates significant distress',
             'RETRACTIONS: subcostal, intercostal, suprasternal, supraclavicular — grade by location',
             '>Suprasternal = upper airway; subcostal = lower airway; multiple locations = severe',
             'GRUNTING: auto-PEEP — child is trying to maintain alveolar recruitment',
             '>Grunting in an infant = serious lower respiratory disease until proven otherwise',
             'HEAD BOBBING: accessory muscle use in infants — sign of severe distress',
             'TRIPOD/SNIFFING POSITION: child optimizing their own airway',
             'SEE-SAW (paradoxical) breathing: chest wall moves opposite to abdomen = impending failure',
         ],
         'notes': 'Each sign corresponds to a level of severity. Grunting is probably the most underrecognized red flag — it means the child is generating positive end-expiratory pressure to keep alveoli open. See-saw breathing means the diaphragm is doing all the work and the intercostal muscles have failed — this child needs immediate respiratory support.'},

        {'type': 'table', 'title': 'Oxygen Delivery Devices and Flow Rates',
         'headers': ['Device', 'Flow Rate', 'FiO2 Delivered', 'Best For'],
         'rows': [
             ['Nasal cannula', '0.25-4 L/min', '24-40%', 'Mild hypoxia, well-tolerated'],
             ['Simple face mask', '6-10 L/min', '35-55%', 'Moderate hypoxia'],
             ['Non-rebreather (NRB)', '10-15 L/min', '60-95%', 'Severe hypoxia, pre-intubation O2'],
             ['High-flow nasal cannula', '1-2 L/kg/min (max 60 L)', 'Titrated 21-100%', 'Moderate-severe; provides PEEP/dead space washout'],
             ['CPAP/BiPAP', 'Variable', 'Titrated', 'Respiratory failure, avoid intubation'],
             ['BVM + reservoir', '15 L/min', '~100%', 'Apnea, pre-oxygenation, active ventilation'],
         ],
         'notes': 'HFNC has revolutionized PEM respiratory care. It provides humidified, heated oxygen at high flow rates, generating 2-5 cmH2O of PEEP at 2 L/kg/min. Start at 1-2 L/kg/min and titrate FiO2 to keep SpO2 ≥94%. HFNC reduces intubation rates in bronchiolitis and may help in status asthmaticus. Know the flow rates for your department and the maximum capacity of your devices.'},

        {'type': 'content', 'title': 'High-Flow Nasal Cannula (HFNC) — PEM Game Changer',
         'bullets': [
             'Provides humidified, heated oxygen at high flow rates',
             'Generates positive pressure (2-5 cmH2O CPAP effect) at 2 L/kg/min',
             'Washes out nasopharyngeal dead space — improves ventilation efficiency',
             'Better tolerated than face masks in young children',
             'Indications: bronchiolitis, pneumonia, asthma (adjunct), post-extubation',
             'Starting flow: 1-2 L/kg/min; FiO2 titrated to SpO2 ≥94%',
             'Escalation criteria: if FiO2 >0.6 or flow maxed → consider NIV or intubation',
             '>HFNC is NOT a substitute for intubation in true respiratory failure',
         ],
         'notes': 'HFNC is the biggest advance in PEM respiratory support in the last decade. Evidence is strongest for bronchiolitis (Franklin et al., NEJM 2018). The key is knowing when HFNC is failing — if FiO2 needs keep rising or the child is not improving within 60-90 minutes, escalate. Do not leave a child on HFNC while they tire out and arrest.'},

        {'type': 'two_column', 'title': 'Respiratory Distress vs Respiratory Failure',
         'left_title': 'RESPIRATORY DISTRESS (Compensating)',
         'left': [
             'Tachypnea with adequate air movement',
             'Retractions but maintaining oxygenation',
             'Alert, interactive, crying',
             'SpO2 ≥94% on room air or low-flow O2',
             'Normal mental status',
             'Strong respiratory effort',
             'Management: treat cause, support oxygenation',
             'WATCH CLOSELY — can progress',
         ],
         'right_title': 'RESPIRATORY FAILURE (Decompensating)',
         'right': [
             'Decreased air movement despite effort',
             'Diminishing retractions ("tiring out")',
             'Altered mental status, lethargy',
             'SpO2 <90% despite O2 supplementation',
             'Grunting, head bobbing, see-saw breathing',
             'Bradycardia (ominous pre-arrest sign)',
             'Management: INTERVENE NOW — BVM, intubation',
             'This child WILL ARREST without action',
         ],
         'notes': 'This is the critical distinction for the PEM fellow. Respiratory distress = the child is working hard and compensating. Respiratory failure = the compensatory mechanisms are failing. The transition from distress to failure can be subtle and rapid. The child who was screaming and fighting the mask but suddenly becomes quiet is NOT getting better — they are decompensating.'},

        {'type': 'content', 'title': 'When to Intubate — Decision Framework',
         'bullets': [
             'Apnea or inadequate respiratory effort',
             'Failure to maintain SpO2 >90% despite maximal non-invasive support',
             'GCS ≤8 or loss of protective airway reflexes',
             'Impending complete airway obstruction',
             'Clinical trajectory worsening despite aggressive management',
             'Need for controlled ventilation (severe TBI, status epilepticus)',
             '>Rule of thumb: if you are THINKING about intubation, prepare for it NOW',
             'The best intubation is the one you prepare for, not the one you are forced into',
         ],
         'notes': 'There is no single SpO2 or blood gas value that mandates intubation — it is a clinical decision based on trajectory. A child whose SpO2 is 92% but improving on HFNC is different from one whose SpO2 is 94% but has been declining for 2 hours. Always prepare early — have your equipment ready and your team briefed before the child decompensates.'},

        {'type': 'content', 'title': 'CXR Interpretation Pearls in PEM',
         'bullets': [
             'NORMAL CXR does not exclude significant disease (early pneumonia, foreign body)',
             'Hyperinflation: flattened diaphragms, >8 posterior ribs visible → air trapping (asthma, bronchiolitis)',
             'Lobar consolidation: bacterial pneumonia most likely',
             'Diffuse bilateral infiltrates: viral pneumonia, pulmonary edema, ARDS',
             'Mediastinal shift: tension pneumothorax, large effusion, or mass',
             'Retrocardiac opacity: commonly missed left lower lobe pneumonia',
             '>Always look at the lateral decubitus or CT if concerned for effusion/empyema',
         ],
         'notes': 'CXR interpretation is a core PEM skill. The most commonly missed finding is a retrocardiac left lower lobe pneumonia — always look behind the heart on the PA view. Round pneumonia in young children can mimic a mass. In asthma, CXR is rarely helpful and should not be routine unless you suspect complication.'},

        {'type': 'case', 'title': '18-Month-Old with Tachypnea and Grunting',
         'scenario': 'An 18-month-old girl presents with 3 days of cough, runny nose, and fever (39.5°C). Today she is breathing fast and "making a funny noise." On exam: RR 58, HR 165, SpO2 91% on room air, moderate subcostal retractions, audible grunting, diminished air entry in the right base, dullness to percussion right lower zone. No wheezing. She is irritable but consolable.',
         'questions': [
             'Localization? (Lower airway / parenchymal — right-sided based on exam)',
             'What does grunting tell you? (Alveolar disease — pneumonia with likely effusion)',
             'Initial management? (O2 via HFNC, CXR, labs, empiric antibiotics)',
             'CXR shows right lower lobe consolidation with moderate effusion — next steps?',
         ],
         'notes': 'This case illustrates a straightforward pneumonia with parapneumonic effusion. The grunting is the key sign pointing to alveolar disease. Dullness to percussion suggests effusion. Initial management includes respiratory support and empiric antibiotics. Ultrasound can characterize the effusion. If large or complex, involve surgery for drainage.'},

        {'type': 'pitfalls', 'title': 'Respiratory Distress Pitfalls',
         'items': [
             'Attributing ALL wheeze to asthma — consider foreign body, cardiac, anaphylaxis',
             'Missing a foreign body: acute onset in a toddler with no URI prodrome',
             'Delayed escalation: leaving a child on HFNC too long while they tire out',
             'Not checking SpO2 on room air before discharging — some children are masked by O2',
             'Forgetting to consider cardiac causes in the infant with tachypnea and hepatomegaly',
             'Routine CXR in obvious viral bronchiolitis or mild asthma — low yield, high cost',
             'Not reassessing after treatment — the child who isn\'t improving needs a new plan',
         ],
         'notes': 'The foreign body pitfall deserves emphasis. A previously well toddler with sudden-onset cough or wheeze should be considered for foreign body until proven otherwise, especially without a viral prodrome. The cardiac pitfall is important: an infant with tachypnea, hepatomegaly, and poor feeding may have CHF, not pneumonia.'},
    ],
    'takeaways': [
        'Localize first: stridor = upper airway; wheeze = lower airway; grunting = alveolar disease',
        'Respiratory failure is identified by CLINICAL signs, not just SpO2 — watch the trajectory',
        'HFNC has transformed PEM respiratory care but is not a substitute for intubation when needed',
        'Always consider foreign body in toddlers with acute-onset respiratory symptoms',
        'Reassess after every intervention — if the child is not improving, change the plan',
    ],
    'references': [
        'Franklin D, et al. A randomized trial of high-flow oxygen therapy in infants with bronchiolitis. N Engl J Med. 2018;378(12):1121-1131.',
        'Ralston SL, et al. Clinical practice guideline: diagnosis, management, and prevention of bronchiolitis. Pediatrics. 2014;134(5):e1474-e1502.',
        'Expert Panel Report 3 (EPR-3): Guidelines for the Diagnosis and Management of Asthma. NIH/NHLBI, 2007.',
        'Topjian AA, Raymond TT, et al. Pediatric BLS and ALS: 2020 AHA Guidelines. Circulation. 2020;142(16_suppl_2).',
        'Fleisher GR, Ludwig S. Textbook of Pediatric Emergency Medicine, 7th Edition. Wolters Kluwer, 2020.',
        'Wing R, et al. Use of high-flow nasal cannula support in the emergency department. Pediatrics. 2012;130(5):e1391-e1396.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '05_respiratory_distress.pptx'))
