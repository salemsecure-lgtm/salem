#!/usr/bin/env python3
"""Build Deck 20: Meningitis, Encephalitis, and CNS Infections"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 20,
    'short_title': 'CNS Infections',
    'title': 'Meningitis, Encephalitis, and CNS Infection Emergencies',
    'subtitle': 'Rapid Diagnosis, Empiric Therapy, and Time-Critical Decision-Making',
    'objectives': [
        'Recognize clinical features of bacterial meningitis at different ages',
        'Interpret CSF results and differentiate bacterial from viral meningitis',
        'Initiate empiric antibiotics rapidly — time to antibiotics affects outcomes',
        'Identify encephalitis and understand the role of empiric acyclovir',
        'Apply the Bacterial Meningitis Score to guide disposition of well-appearing children with pleocytosis',
    ],
    'slides': [
        {'type': 'content', 'title': 'CNS Infections in PEM: Time-Critical Diagnoses',
         'bullets': [
             'Bacterial meningitis: mortality 5-10% even with treatment; 20-30% have long-term sequelae',
             'HSV encephalitis: mortality 70% if untreated; early acyclovir reduces to 20-30%',
             'The PEM role: recognize, empirically treat, and not delay — time to antibiotics matters',
             'Classic presentation in older children: fever, headache, neck stiffness, photophobia',
             'In infants: presentation is nonspecific — irritability, poor feeding, bulging fontanelle, lethargy',
             'Kernig and Brudzinski signs: specific but not sensitive — absence does NOT rule out meningitis',
             '>If you suspect meningitis, start antibiotics BEFORE LP. Never delay treatment for diagnosis.',
         ],
         'notes': 'Open with the mortality data to establish urgency. Bacterial meningitis is one of the few diagnoses where minutes matter. Every hour of delayed antibiotics increases mortality and morbidity. The classic triad of fever, neck stiffness, and altered mental status is present in only 44% of adults and even less reliably in children.'},

        {'type': 'table', 'title': 'Common Pathogens by Age',
         'headers': ['Age Group', 'Bacterial', 'Viral', 'Other'],
         'rows': [
             ['0-28 days', 'GBS, E. coli, Listeria', 'HSV, Enterovirus', 'Congenital CMV, Toxoplasma'],
             ['1-3 months', 'GBS, E. coli, S. pneumo, Listeria', 'Enterovirus, HSV, Parechovirus', ''],
             ['3 months-5 years', 'S. pneumoniae (#1), N. meningitidis', 'Enterovirus, EBV, arboviruses', ''],
             ['5-18 years', 'N. meningitidis, S. pneumoniae', 'Enterovirus, HSV-1, EBV, arboviruses', 'Mycoplasma (ADEM)'],
             ['Immunocompromised', 'Listeria, GNR, S. pneumo, Cryptococcus', 'CMV, JC virus', 'TB, fungal'],
         ],
         'notes': 'S. pneumoniae is now the #1 cause of bacterial meningitis across most age groups (after widespread Hib and meningococcal vaccination). N. meningitidis remains important, especially in adolescents and in outbreak settings. Listeria must be covered in neonates and immunocompromised patients. HSV is the most important viral cause to identify because it is treatable.'},

        {'type': 'algorithm', 'title': 'Approach to Suspected Meningitis in the ED',
         'steps': [
             'RECOGNIZE: fever + AMS, irritability, neck stiffness, headache, bulging fontanelle, petechiae',
             'STABILIZE: ABCs, IV access, point-of-care glucose',
             'EMPIRIC ANTIBIOTICS IMMEDIATELY if ill-appearing (do NOT wait for LP)',
             'LP if safe: send CSF for cell count, glucose, protein, gram stain, culture, HSV PCR, enterovirus PCR',
             'CT before LP only if: focal deficits, papilledema, GCS ≤12, or concern for mass/increased ICP',
             'DEXAMETHASONE: 0.15 mg/kg IV q6h × 2 days — give with or before first antibiotic dose (if S. pneumo suspected)',
             'Monitor: serial neuro exams, ICP management if needed, isolation precautions',
         ],
         'notes': 'The most important step is #3: antibiotics first. If you are worried enough to think about meningitis, give antibiotics. You can always stop them if the CSF is normal. You cannot undo the harm of delayed treatment. CT before LP is only needed for specific indications — most children do not need CT before LP. Dexamethasone improves outcomes for S. pneumoniae meningitis in children.'},

        {'type': 'table', 'title': 'CSF Interpretation',
         'headers': ['Parameter', 'Normal', 'Bacterial Meningitis', 'Viral Meningitis', 'HSV Encephalitis'],
         'rows': [
             ['WBC', '<5 cells/µL (neonates <20)', '>1000 (PMN predominant)', '10-500 (lymph predominant)', '10-500 (lymph predominant)'],
             ['Glucose', '>40 mg/dL (or >60% serum)', '<40 (or <60% serum)', 'Normal', 'Normal or low'],
             ['Protein', '<45 mg/dL (neonates <170)', '>100 mg/dL', 'Mildly elevated (50-100)', 'Elevated (50-200)'],
             ['Gram Stain', 'Negative', 'Positive in 60-90%', 'Negative', 'Negative'],
             ['Opening Pressure', '<20 cm H2O', 'Often elevated', 'Normal or mildly elevated', 'Often elevated'],
             ['RBCs', '<5', 'Usually low (unless traumatic)', 'Usually low', 'Often elevated (hemorrhagic)'],
         ],
         'notes': 'CSF interpretation is a core skill. Bacterial meningitis classically shows: high WBC with PMN predominance, low glucose, high protein, positive gram stain. Viral meningitis: moderate WBC with lymphocyte predominance, normal glucose, mildly elevated protein. HSV encephalitis: lymphocytic pleocytosis with RBCs (temporal lobe hemorrhagic necrosis). But early bacterial meningitis can look viral — treat based on clinical suspicion, not just CSF results.'},

        {'type': 'key_point', 'title': 'Bacterial Meningitis Score',
         'point': 'In children with CSF pleocytosis, the BMS identifies those at very low risk for bacterial meningitis.',
         'sub': 'Criteria for LOW risk (BMS = 0): CSF gram stain negative, CSF ANC <1000, CSF protein <80, peripheral ANC <10,000, NO seizure at presentation. If BMS = 0, risk of bacterial meningitis is 0.1%. Consider outpatient management with close follow-up for well-appearing children.',
         'notes': 'The Bacterial Meningitis Score (Nigrovic et al., Pediatrics 2002, validated in multiple studies) is extremely useful for the well-appearing child with CSF pleocytosis. If all criteria are met (BMS = 0), the child almost certainly has viral meningitis and may be managed as an outpatient with close follow-up and pending cultures. This reduces unnecessary hospitalizations.'},

        {'type': 'table', 'title': 'Empiric Antibiotic Regimens for Meningitis',
         'headers': ['Age', 'Empiric Regimen', 'Key Notes'],
         'rows': [
             ['0-28 days', 'Ampicillin + Cefotaxime (or Gent) + Acyclovir', 'Covers GBS, E. coli, Listeria, HSV'],
             ['1-3 months', 'Vancomycin + Ceftriaxone (or Cefotaxime) + Ampicillin', 'Add ampicillin for Listeria; acyclovir if HSV concern'],
             ['3 months-18 years', 'Vancomycin + Ceftriaxone + Dexamethasone', 'Vancomycin for resistant S. pneumo; dexa 0.15 mg/kg q6h × 2 days'],
             ['Immunocompromised', 'Vancomycin + Ceftriaxone + Ampicillin ± Acyclovir', 'Broaden for Listeria and atypical organisms'],
         ],
         'notes': 'Vancomycin is added for resistant S. pneumoniae (increasing prevalence). Ceftriaxone is preferred over cefotaxime in most settings (once-daily dosing, widely available). Dexamethasone should be given with or before the first antibiotic dose — it reduces hearing loss and neurologic sequelae from pneumococcal meningitis. For neonates, many experts now prefer cefotaxime over ceftriaxone to avoid bilirubin displacement.'},

        {'type': 'content', 'title': 'Encephalitis: Recognition and Empiric Treatment',
         'bullets': [
             'Encephalitis = brain parenchymal inflammation — presents with AMS, seizures, focal deficits',
             'Unlike meningitis (meningeal), encephalitis involves brain tissue itself',
             'HSV-1 is the most important treatable cause — temporal lobe predilection',
             'Clues: personality changes, temporal lobe seizures, focal deficits, hemorrhagic CSF',
             'MRI: temporal lobe signal abnormality (T2/FLAIR hyperintensity) — sensitive but not immediate',
             'Empiric acyclovir 20 mg/kg IV q8h (neonates) or 10 mg/kg IV q8h (children) — start immediately',
             'Other causes: arboviruses (West Nile, Eastern equine), Mycoplasma, autoimmune (anti-NMDA receptor)',
             '>Any child with fever + altered mental status + seizures → empiric acyclovir until HSV is ruled out',
         ],
         'notes': 'Encephalitis is differentiated from meningitis by the presence of altered mental status and/or focal neurologic deficits. HSV encephalitis is the most important diagnosis to make because it is TREATABLE — untreated mortality is 70%. Start acyclovir immediately and send CSF HSV PCR. MRI is more sensitive than CT but should not delay treatment.'},

        {'type': 'case', 'title': '7-Year-Old with Fever, Headache, and Neck Pain',
         'scenario': 'A 7-year-old girl presents with 2 days of fever (39.5°C), severe headache, vomiting, and neck stiffness. She is irritable and photophobic but alert and oriented. No rash, no seizures. Positive Brudzinski sign. No focal deficits. LP performed: WBC 450 (85% lymphocytes), glucose 55 (serum 95), protein 65, gram stain negative.',
         'questions': [
             'Bacterial vs. viral meningitis? (CSF pattern suggests viral: lymphocyte predominant, normal glucose, mildly elevated protein, negative gram stain)',
             'Calculate BMS: gram stain negative ✓, CSF ANC <1000 ✓ (450 × 0.15 = 68), protein <80 ✓, no seizure ✓. Need peripheral ANC.',
             'If peripheral ANC <10,000 → BMS = 0 → very low risk for bacterial meningitis',
             'Plan: empiric ceftriaxone + vancomycin pending cultures; if BMS = 0 and well-appearing → consider discharge with 24h follow-up',
         ],
         'notes': 'This case demonstrates real-time BMS application. The CSF is clearly not normal, but the pattern suggests viral meningitis. The BMS helps you decide: admit and treat aggressively, or observe/discharge with close follow-up? If BMS = 0 and the child is well-appearing, outpatient management with 24-hour follow-up is supported by evidence. Many physicians still admit for observation — this is also acceptable.'},

        {'type': 'pitfalls', 'title': 'CNS Infection Pitfalls',
         'items': [
             'Delaying antibiotics for LP — if meningitis is suspected, antibiotics FIRST',
             'Getting CT before LP when not indicated — most children with suspected meningitis do NOT need CT first',
             'Forgetting dexamethasone — must be given with or before first antibiotic dose (not after)',
             'Not sending HSV PCR on CSF — missing treatable HSV encephalitis is devastating',
             'Relying on negative gram stain to rule out bacterial meningitis — sensitivity is only 60-90%',
             'Missing partially treated meningitis — prior antibiotics can sterilize cultures but the child still has meningitis',
             'Not considering autoimmune encephalitis (anti-NMDA receptor) in adolescents with psychiatric symptoms + seizures',
         ],
         'notes': 'The dexamethasone timing is critical: it must be given before or with the first antibiotic dose. If given after antibiotics have already been administered, the benefit is reduced. The HSV PCR pearl: it can be false-negative in the first 72 hours of symptoms. If clinical suspicion is high and initial PCR is negative, repeat in 3-5 days.'},
    ],
    'takeaways': [
        'If you suspect meningitis, give empiric antibiotics IMMEDIATELY — do not wait for LP',
        'Dexamethasone (0.15 mg/kg q6h × 2 days) should be given with or before the first antibiotic dose',
        'The Bacterial Meningitis Score (BMS = 0) identifies children with CSF pleocytosis at very low risk for bacterial meningitis',
        'HSV encephalitis: fever + AMS + seizures → empiric acyclovir (20 mg/kg q8h neonates, 10 mg/kg q8h children)',
        'CSF interpretation: low glucose + high PMNs = bacterial until proven otherwise; treat aggressively',
    ],
    'references': [
        'Nigrovic LE, et al. Clinical prediction rule for identifying children with cerebrospinal fluid pleocytosis at very low risk of bacterial meningitis. JAMA. 2007;297(1):52-60.',
        'Tunkel AR, et al. Practice guidelines for the management of bacterial meningitis. Clin Infect Dis. 2004;39(9):1267-1284.',
        'Kimberlin DW, et al. Natural history of neonatal herpes simplex virus infections in the acyclovir era. Pediatrics. 2001;108(2):223-229.',
        'Van de Beek D, et al. Corticosteroids for acute bacterial meningitis. Cochrane Database Syst Rev. 2015;9:CD004405.',
        'Venkatesan A, et al. Case definitions, diagnostic algorithms, and priorities in encephalitis: consensus statement of the International Encephalitis Consortium. Clin Infect Dis. 2013;57(8):1114-1128.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '20_meningitis_encephalitis.pptx'))
