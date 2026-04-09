#!/usr/bin/env python3
"""Build Deck 25: Child Abuse Recognition in the ED"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 25,
    'short_title': 'Child Abuse Recognition',
    'title': 'Child Abuse Recognition in the ED',
    'subtitle': 'Patterns of Injury, High-Risk Presentations, and the PEM Physician\'s Role',
    'objectives': [
        'Recognize injury patterns that are concerning for non-accidental trauma (NAT)',
        'Identify high-risk presentations: bruising in non-mobile infants, specific fracture patterns, AHT',
        'Apply the TEN-4 bruising rule and understand its evidence base',
        'Understand the required workup: skeletal survey, labs, ophthalmology, neuroimaging',
        'Navigate the reporting process and understand your mandatory reporting obligation',
    ],
    'slides': [
        {'type': 'content', 'title': 'Child Abuse in PEM: A Must-Recognize Diagnosis',
         'bullets': [
             'Child maltreatment affects ~1 in 7 children in the US annually',
             '~1,800 children die from abuse/neglect each year in the US — most are <3 years old',
             'The ED is often the first and sometimes the only opportunity to identify abuse',
             'Missing abuse means returning a child to a dangerous environment — 25-50% will be re-injured',
             'Identifying abuse is not about blame — it is about child protection',
             '>You are legally MANDATED to report suspected child abuse. You do not need to prove it — you need to suspect it.',
         ],
         'notes': 'This is a sensitive but critical topic. Open by stating that the goal is not to accuse families but to protect children. Frame the ED visit as a window of opportunity. Data show that up to 30% of children who die from abuse had prior contact with the healthcare system where the abuse was not recognized. We can do better.'},

        {'type': 'table', 'title': 'TEN-4 Bruising Rule',
         'headers': ['Region', 'Rule', 'Significance'],
         'rows': [
             ['T — Torso (chest, abdomen, back, buttocks)', 'ANY bruise in child <4 years', 'Concerning for abuse'],
             ['E — Ears (any part of the ear)', 'ANY bruise in child <4 years', 'Concerning for abuse'],
             ['N — Neck (any part)', 'ANY bruise in child <4 years', 'Concerning for abuse'],
             ['4 — Any bruise in child <4 MONTHS', 'ANY bruise ANYWHERE', '"Those who don\'t cruise rarely bruise"'],
         ],
         'notes': 'The TEN-4 rule (Pierce et al., Pediatrics 2010) has excellent sensitivity (97%) for identifying abusive bruising. The key concept: bruises on the torso, ears, and neck are UNCOMMON in accidental injury and highly associated with abuse. And any bruise in a non-mobile infant (<4 months, before cruising) is suspicious — these babies cannot generate bruising force on their own.'},

        {'type': 'content', 'title': 'High-Risk Fracture Patterns',
         'bullets': [
             'Classic metaphyseal lesions (CMLs / "corner fractures" / "bucket handle"): highly specific for abuse',
             'Rib fractures in infants (especially posterior): highly specific for abuse (require squeezing force)',
             'Multiple fractures in different stages of healing: strongly suggests repeated injury',
             'Skull fractures: complex, bilateral, crossing suture lines, or depressed → concerning',
             'Femur fractures in non-ambulatory children: any long bone fracture in a non-walking child is suspicious',
             'Spiral fractures of the humerus in children <15 months: concerning for twisting mechanism',
             '>The younger the child and the more unusual the fracture pattern, the higher the suspicion for NAT',
         ],
         'notes': 'Fracture specificity for abuse varies. CMLs and posterior rib fractures are the most specific. A single linear skull fracture can be accidental (short fall), but multiple skull fractures or complex patterns suggest greater force. Any long bone fracture in a non-ambulatory infant (before pulling to stand) requires a thorough evaluation for NAT.'},

        {'type': 'two_column', 'title': 'Accidental vs. Inflicted Injury Features',
         'left_title': 'LIKELY ACCIDENTAL',
         'left': [
             'Consistent, plausible history matching injury',
             'Developmentally appropriate mechanism',
             'Bruises on bony prominences (shins, forehead, knees)',
             'Mobile child with witnessed fall',
             'Single, simple injury pattern',
             'Appropriate concern and help-seeking behavior',
             'Pattern consistent with known mechanism',
         ],
         'right_title': 'CONCERNING FOR NAT',
         'right': [
             'History inconsistent with injury pattern or severity',
             'Changing or evolving story',
             'Bruises on soft-tissue areas (TEN-4: torso, ears, neck)',
             'Non-mobile infant with any bruise or fracture',
             'Multiple injuries in different stages of healing',
             'Delay in seeking medical attention',
             'Patterned injuries (belt marks, bite marks, cigarette burns)',
             'Injuries to bilateral or symmetric body regions',
         ],
         'notes': 'The history-injury mismatch is the single most important red flag. When a parent says "he rolled off the couch" but the child has multiple rib fractures, the mechanism does not explain the injury. Developmentally inappropriate mechanism is equally important: a 2-month-old cannot roll off anything because they cannot roll. Document the history carefully — use direct quotes.'},

        {'type': 'content', 'title': 'Abusive Head Trauma (AHT)',
         'bullets': [
             'Leading cause of traumatic death in infants — peak age: 2-4 months',
             'Classic triad: subdural hematomas + retinal hemorrhages + encephalopathy',
             'Mechanism: violent shaking ± impact (high angular acceleration forces)',
             'Presentation: altered mental status, seizures, apnea, vomiting, irritability, bulging fontanelle',
             'Often NO external signs of trauma — the head may look completely normal externally',
             'CT findings: bilateral subdural hematomas (especially interhemispheric), SAH, cerebral edema',
             'Retinal hemorrhages: present in >85% of AHT; multilayered and extending to periphery = highly specific',
             '>Any infant with unexplained altered mental status, seizures, or apnea → consider AHT',
         ],
         'notes': 'AHT is the most lethal form of child abuse. The classic presentation is an infant brought in with altered mental status or seizures with no or inconsistent history of trauma. The caregiver often reports "found not breathing" or "was fine then suddenly stopped moving." The retinal exam by ophthalmology is critical — multilayered, widespread hemorrhages are very specific for AHT.'},

        {'type': 'algorithm', 'title': 'ED Workup When Abuse Is Suspected',
         'steps': [
             'PHYSICAL: Complete skin exam (undress fully), document all injuries with measurements, photographs',
             'SKELETAL SURVEY: AP views of all long bones, AP/lateral skull, AP/lateral spine, AP chest/pelvis (for ALL children <2 years when abuse suspected)',
             'LABS: CBC, CMP, coagulation studies (PT/PTT/fibrinogen), lipase, amylase, LFTs (AST/ALT), UA',
             'NEUROIMAGING: CT head (acute) ± MRI (subacute/chronic findings) — for any concern for AHT',
             'OPHTHALMOLOGY: Dilated fundoscopic exam — for any concern for AHT',
             'SOCIAL WORK: Involve immediately; contact child protective services (CPS)',
             'DOCUMENTATION: Detailed, objective, factual documentation of history, exam, and all injuries',
         ],
         'notes': 'The workup must be comprehensive. Labs help identify occult injuries (elevated LFTs suggest abdominal trauma, elevated lipase suggests pancreatic injury) and rule out medical mimics (coagulopathy). Skeletal survey is indicated for ALL children <2 years when abuse is suspected. Follow-up skeletal survey at 2 weeks detects additional fractures in 10-30% of cases (healing fractures become more visible).'},

        {'type': 'content', 'title': 'Medical Mimics of Child Abuse',
         'bullets': [
             'Bleeding disorders: hemophilia, von Willebrand disease, ITP → easy bruising',
             'Osteogenesis imperfecta: brittle bones → multiple fractures with minimal trauma',
             'Mongolian spots: may be mistaken for bruises — document location and appearance',
             'Ehlers-Danlos syndrome: skin fragility, easy bruising',
             'Henoch-Schönlein purpura: purpuric rash on buttocks and lower extremities',
             'Cultural practices: coining, cupping — may mimic patterned bruising',
             'Birth trauma: clavicle fractures, cephalohematoma',
             '>Rule out medical causes, but do NOT let a medical workup delay reporting if suspicion is high',
         ],
         'notes': 'Always consider medical mimics — but do not let the search for a medical explanation become a reason to delay CPS referral. You can evaluate for OI and bleeding disorders while simultaneously reporting to CPS. Coagulation studies (PT, PTT, fibrinogen) should be sent in all cases to evaluate for bleeding disorders. The combination of injuries with a NORMAL coagulation panel is very concerning.'},

        {'type': 'case', 'title': '4-Month-Old with Bruise on the Cheek',
         'scenario': 'A 4-month-old boy is brought in for a "cold" by his mother. During the exam, you notice a 2 cm bruise on his right cheek. Mother says "he must have bumped into something in his crib." The infant is not yet rolling. He appears well, is afebrile, and the rest of the exam is normal. He lives with his mother and her boyfriend.',
         'questions': [
             'Is this bruise concerning? (YES — TEN-4: any bruise in a child <4 months is concerning)',
             'Is the history plausible? (NO — a 4-month-old who is not rolling cannot generate bruising force from a crib)',
             'Workup? (Skeletal survey, CT head, ophthalmology consult, CBC, coagulation studies, LFTs, lipase)',
             'Social work and CPS referral? (YES — mandatory reporting based on reasonable suspicion)',
         ],
         'notes': 'This case is designed to teach the TEN-4 rule in action. A bruise on the cheek of a non-mobile infant is abuse until proven otherwise. The history does not match the developmental stage. The workup is comprehensive because if there is one visible injury, there may be hidden ones (fractures, intracranial hemorrhage, abdominal organ injury). Mandatory reporting does not require certainty — it requires reasonable suspicion.'},

        {'type': 'pitfalls', 'title': 'Child Abuse Recognition Pitfalls',
         'items': [
             'Accepting implausible mechanisms for injuries — "he fell off the couch" does not cause rib fractures',
             'Not examining the entire skin surface — injuries hidden under clothing, in the scalp, in the mouth',
             'Missing bruises in non-mobile infants — "those who don\'t cruise rarely bruise"',
             'Not ordering skeletal survey in children <2 years with suspicious injuries',
             'Allowing concern about "false accusations" to prevent reporting — you are not the investigator, CPS is',
             'Not documenting with detail — use body diagrams, measurements, photographs, and direct quotes',
             'Diagnosing abuse without considering medical mimics — always send coagulation studies',
         ],
         'notes': 'The biggest barrier to child abuse recognition is cognitive: the reluctance to consider that a caregiver could harm a child. Combat this with systematic evaluation. Apply the same objective approach you use for any other diagnosis: collect data, analyze the mechanism, determine if the history fits the injury. Documentation is both clinically and legally essential.'},
    ],
    'takeaways': [
        'TEN-4: bruises on Torso, Ears, Neck in children <4 years, or ANY bruise in children <4 months = concerning for abuse',
        'History-injury mismatch is the single most important red flag for non-accidental trauma',
        'AHT: subdural hematomas + retinal hemorrhages + encephalopathy in an infant = abusive head trauma until proven otherwise',
        'Mandatory reporting: you need reasonable suspicion, not proof — report to CPS and let them investigate',
        'Skeletal survey + labs + neuroimaging + ophthalmology = standard abuse workup in children <2 years',
    ],
    'references': [
        'Pierce MC, et al. Bruising characteristics discriminating physical child abuse from accidental trauma. Pediatrics. 2010;125(1):67-74. (TEN-4)',
        'Christian CW, et al. Abusive head trauma in infants and children. Pediatrics. 2009;123(5):1409-1411.',
        'Lindberg DM, et al. Testing for abuse in children with sentinel injuries. Pediatrics. 2015;136(5):831-838.',
        'Wood JN, et al. Prevalence of abuse among young children with femur fractures. BMC Pediatr. 2014;14:169.',
        'Flaherty EG, et al. Evaluating children with fractures for child physical abuse. Pediatrics. 2014;133(2):e477-e489.',
        'Maguire SA, et al. Which clinical features distinguish inflicted from non-inflicted brain injury? A systematic review. Arch Dis Child. 2009;94(11):860-867.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '25_child_abuse_recognition.pptx'))
