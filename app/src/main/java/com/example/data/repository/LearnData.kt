package com.example.data.repository

data class LearnArticle(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val iconName: String,
    val summaryEn: String,
    val summaryHi: String,
    val contentEn: List<String>,
    val contentHi: List<String>
)

object LearnData {
    val articles: List<LearnArticle> = listOf(
        LearnArticle(
            id = "how_to_start",
            titleEn = "How to Start Exercising as a Beginner",
            titleHi = "शुरुआती तौर पर व्यायाम कैसे शुरू करें",
            iconName = "PlayArrow",
            summaryEn = "Consistency over intensity. How to build a lasting fitness habit.",
            summaryHi = "कठिनाई से ज्यादा नियमितता जरूरी है। स्थायी आदत कैसे बनाएं।",
            contentEn = listOf(
                "Starting a workout routine doesn't require hours in the gym or heavy equipment. Your own bodyweight is more than enough to build lasting strength and mobility.",
                "Start with small, manageable sessions: 15 to 20 minutes every other day is ideal for giving your muscles and tendons time to adapt.",
                "Focus purely on good form rather than speed or rep counts. Doing 5 clean push-ups is 100 times better than 15 sloppy ones.",
                "Celebrate every completed workout, no matter how short. Consistency is the secret to lifelong health."
            ),
            contentHi = listOf(
                "वर्कआउट शुरू करने के लिए महंगे जिम या भारी उपकरणों की आवश्यकता नहीं है। आपका अपना शरीर ही ताकत और लचीलापन बनाने के लिए पर्याप्त है।",
                "छोटे और सरल सत्रों से शुरुआत करें: एक दिन छोड़कर 15 से 20 मिनट का व्यायाम आपकी मांसपेशियों को अनुकूलित करने के लिए सबसे अच्छा है।",
                "संख्या की बजाय सही मुद्रा (Form) पर ध्यान दें। 5 सही पुश-अप करना 15 गलत पुश-अप से कहीं बेहतर है।",
                "हर पूरे किए गए वर्कआउट का जश्न मनाएं। नियमितता ही आजीवन स्वास्थ्य का असली रहस्य है।"
            )
        ),
        LearnArticle(
            id = "what_is_warmup",
            titleEn = "What is a Warm-Up & Why It Matters",
            titleHi = "वार्म-अप क्या है और यह क्यों जरूरी है?",
            iconName = "Whatshot",
            summaryEn = "Prime your heart, joints, and nervous system to prevent injuries.",
            summaryHi = "चोट से बचने के लिए हृदय और जोड़ों को तैयार करें।",
            contentEn = listOf(
                "Think of your muscles like rubber bands. When cold, stretching or loading them suddenly can cause strains or tears. Warming up lubricates joints with synovial fluid.",
                "A dynamic warm-up increases blood circulation, elevates body temperature, and prepares your nervous system for movement.",
                "Spend 3 to 5 minutes doing arm circles, torso twists, marching high knees, and bodyweight hip hinges.",
                "Never do intense static stretches before a workout—save relaxed stretches for the cool-down."
            ),
            contentHi = listOf(
                "अपनी मांसपेशियों को रबर बैंड की तरह समझें। ठंडी होने पर अचानक खींचने से चोट लग सकती है। वार्म-अप से जोड़ों में चिकनाई आती है।",
                "वार्म-अप से रक्त संचार बढ़ता है, शरीर का तापमान बढ़ता है और तंत्रिका तंत्र सक्रिय होता है।",
                "3 से 5 मिनट हाथों को गोल घुमाने, कमर घुमाने और घुटनों को ऊपर उठाने जैसे हल्के व्यायाम करें।",
                "वर्कआउट से पहले बहुत देर तक एक ही खिंचाव में न रुकें—गहरी स्ट्रेचिंग हमेशा वर्कआउट के बाद करें।"
            )
        ),
        LearnArticle(
            id = "sets_and_reps",
            titleEn = "What are Sets and Reps?",
            titleHi = "सेट्स (Sets) और रैप्स (Reps) क्या होते हैं?",
            iconName = "FitnessCenter",
            summaryEn = "A simple explanation of the building blocks of all fitness programs.",
            summaryHi = "फिटनेस की मूल भाषा का सरल और स्पष्ट विवरण।",
            contentEn = listOf(
                "Reps (short for Repetitions): One complete cycle of an exercise. For example, lowering down into a squat and standing back up equals 1 rep.",
                "Sets: A group of consecutive reps performed together before taking a rest. Doing 10 squats in a row is 1 set of 10 reps.",
                "Example: '3 sets of 10 reps' means you do 10 squats, rest for 45 seconds, do another 10, rest, and do your final 10 squats.",
                "For beginners, 2 to 3 sets of 8 to 12 reps is the sweet spot for learning motor patterns and building muscle tone."
            ),
            contentHi = listOf(
                "रैप्स (Repetitions): किसी व्यायाम का एक पूरा चक्कर। उदाहरण के लिए, एक बार उठक-बैठक (स्क्वाट) करके वापस खड़ा होना 1 रैप कहलाता है।",
                "सेट्स (Sets): बिना रुके किए जाने वाले रैप्स का समूह। जैसे एक बार में लगातार 10 स्क्वाट करना 1 सेट कहलाता है।",
                "उदाहरण: '3 सेट्स, 10 रैप्स' का मतलब है कि आप 10 स्क्वाट करेंगे, 45 सेकंड आराम करेंगे, फिर 10 करेंगे, आराम करेंगे और अंत में 10 और करेंगे।",
                "शुरुआती लोगों के लिए 2 से 3 सेट्स और 8 से 12 रैप्स सबसे उपयुक्त हैं।"
            )
        ),
        LearnArticle(
            id = "how_long_to_rest",
            titleEn = "How Long Should I Rest Between Sets?",
            titleHi = "सेट्स के बीच कितनी देर आराम करना चाहिए?",
            iconName = "Timer",
            summaryEn = "Optimal rest times for strength, recovery, and steady progress.",
            summaryHi = "मांसपेशियों की रिकवरी और निरंतर प्रगति के लिए सही आराम समय।",
            contentEn = listOf(
                "Resting between sets allows your cellular energy systems (ATP and glycogen) to replenish so your next set is just as effective.",
                "Beginner Recommendation: Rest 45 to 60 seconds between bodyweight sets.",
                "If you feel lightheaded, out of breath, or your heart is pounding, extend your rest to 75-90 seconds. Listen to your body.",
                "Don't sit completely stationary during rest—walk gently around the room and take smooth, deep breaths."
            ),
            contentHi = listOf(
                "सेट्स के बीच आराम करने से शरीर की ऊर्जा फिर से बनती है ताकि अगला सेट भी अच्छी तरह हो सके।",
                "शुरुआती सुझाव: बॉडीवेट व्यायाम के बीच 45 से 60 सेकंड का आराम सबसे सही है।",
                "यदि सांस फूल रही हो या चक्कर जैसा लगे, तो आराम का समय 75-90 सेकंड तक बढ़ा लें। अपने शरीर की सुनें।",
                "आराम के समय एक जगह बैठे न रहें—कमरे में धीरे-धीरे टहलें और गहरी सांस लें।"
            )
        ),
        LearnArticle(
            id = "how_to_breathe",
            titleEn = "How to Breathe During Exercise",
            titleHi = "व्यायाम के दौरान सांस कैसे लें?",
            iconName = "Air",
            summaryEn = "Mastering the inhale-exhale rhythm to boost power and avoid dizziness.",
            summaryHi = "चक्कर से बचने और ताकत बढ़ाने के लिए सांस की सही लय।",
            contentEn = listOf(
                "Golden Rule: Never hold your breath during resistance exercises (unless instructed in advanced heavy lifting). Holding your breath spikes blood pressure.",
                "Inhale on the easier phase (eccentric): Breathe in through your nose as you lower down in a push-up or squat.",
                "Exhale on the harder exertion phase (concentric): Blow air out through your mouth as you push up from the floor or stand up from a squat.",
                "In static holds like the Plank: Keep taking steady, rhythmic diaphragmatic breaths throughout."
            ),
            contentHi = listOf(
                "सुनहरा नियम: व्यायाम करते समय सांस कभी न रोकें। सांस रोकने से ब्लड प्रेशर बढ़ सकता है।",
                "आसान चरण में सांस अंदर लें: जैसे पुश-अप या स्क्वाट में नीचे जाते समय नाक से सांस भरें।",
                "कठिन चरण में सांस बाहर छोड़ें: जैसे फर्श से ऊपर उठते समय या सीधे खड़े होते समय मुंह से सांस छोड़ें।",
                "प्लैंक जैसी स्थिर मुद्राओं में: लगातार धीमी और सामान्य सांस लेते रहें।"
            )
        ),
        LearnArticle(
            id = "correct_posture",
            titleEn = "How to Maintain Correct Posture",
            titleHi = "सही शारीरिक मुद्रा (Posture) कैसे बनाए रखें?",
            iconName = "AccessibilityNew",
            summaryEn = "Neutral spine, proud chest, and aligned joints for pain-free workouts.",
            summaryHi = "दर्दमुक्त वर्कआउट के लिए सीधी रीढ़ और सही शारीरिक संतुलन।",
            contentEn = listOf(
                "Neutral Spine: Imagine a straight wooden dowel along your spine touching the back of your head, upper back, and tailbone.",
                "Proud Chest: Keep your collarbones wide and shoulders drawn gently back and down away from your ears.",
                "Joint Stacking: Whenever possible, keep wrists directly beneath shoulders or knees tracking over mid-foot to distribute load evenly.",
                "Core Engagement: Pull your belly button gently inward towards your spine without holding your breath."
            ),
            contentHi = listOf(
                "सीधी रीढ़: कल्पना करें कि आपके सिर के पीछे, ऊपरी पीठ और कमर के निचले हिस्से पर एक सीधी छड़ी रखी है।",
                "छाती खुली रखें: कंधों को कानों से दूर नीचे और हल्का पीछे रखें।",
                "जोड़ों की सीध: कोहनियों और घुटनों को हमेशा सही दिशा में रखें ताकि जोड़ों पर गलत दबाव न पड़े।",
                "पेट को सक्रिय रखें: बिना सांस रोके पेट की मांसपेशियों को हल्का अंदर खींचकर रखें।"
            )
        ),
        LearnArticle(
            id = "how_to_cooldown",
            titleEn = "How to Cool Down Effectively",
            titleHi = "वर्कआउट के बाद सही कूल-डाउन कैसे करें?",
            iconName = "SelfImprovement",
            summaryEn = "Lower your heart rate and release tight muscles with relaxed stretches.",
            summaryHi = "हृदय गति को सामान्य करें और मांसपेशियों की जकड़न दूर करें।",
            contentEn = listOf(
                "Stopping abruptly after vigorous exercise can cause blood pooling in extremities and lightheadedness.",
                "Spend 2 minutes walking at an easy pace until your breathing returns to conversational level.",
                "Follow with gentle static stretches: hold each stretch (hamstrings, quads, chest, lower back) for 20 to 30 seconds.",
                "Drink 1 to 2 glasses of water to begin rehydrating your muscles."
            ),
            contentHi = listOf(
                "वर्कआउट के तुरंत बाद अचानक बैठ जाने से चक्कर आ सकते हैं।",
                "2 मिनट सामान्य गति से टहलें जब तक कि सांस पूरी तरह सामान्य न हो जाए।",
                "इसके बाद हल्की स्ट्रेचिंग करें: छाती, जांघों और पीठ के स्ट्रेच को 20 से 30 सेकंड तक रोककर रखें।",
                "मांसपेशियों में पानी की कमी पूरी करने के लिए 1-2 गिलास पानी पिएं।"
            )
        ),
        LearnArticle(
            id = "beginner_mistakes",
            titleEn = "Common Beginner Exercise Mistakes",
            titleHi = "शुरुआती लोगों की सामान्य गलतियां जिनसे बचें",
            iconName = "Warning",
            summaryEn = "Avoid burnout, joint discomfort, and early plateaus.",
            summaryHi = "अत्यधिक थकान और जोड़ों के दर्द से बचने के उपाय।",
            contentEn = listOf(
                "Mistake 1: Doing too much too soon. Exercising 7 days a week as a novice causes extreme soreness and injury.",
                "Mistake 2: Sacrificing form for quantity. Flaring elbows or bouncing squats stresses joints rather than muscles.",
                "Mistake 3: Skipping rest days. Muscle growth and fat burning actually happen when you rest, not during the workout itself.",
                "Mistake 4: Comparing yourself to fitness influencers. Focus exclusively on your own personal daily improvement."
            ),
            contentHi = listOf(
                "गलती 1: शुरुआत में ही बहुत ज्यादा करना। पहले ही हफ्ते सातों दिन वर्कआउट करने से मांसपेशियों में अत्यधिक दर्द हो सकता है।",
                "गलती 2: ज्यादा गिनती के चक्कर में गलत तरीके से करना। गलत मुद्रा से जोड़ों को नुकसान पहुंचता है।",
                "गलती 3: आराम के दिन छोड़ना। शरीर का विकास और फैट बर्निंग आराम के दौरान ही होती है।",
                "गलती 4: दूसरों से तुलना करना। केवल अपनी दैनिक प्रगति पर ध्यान केंद्रित करें।"
            )
        ),
        LearnArticle(
            id = "how_to_progress",
            titleEn = "How to Progress Safely (Progressive Overload)",
            titleHi = "सुरक्षित तरीके से प्रगति (Progress) कैसे करें?",
            iconName = "TrendingUp",
            summaryEn = "The right way to increase challenge without risking injury.",
            summaryHi = "बिना चोट के जोखिम के धीरे-धीरे चुनौती कैसे बढ़ाएं।",
            contentEn = listOf(
                "Progressive Overload means gradually increasing the stimulus on your body over time so you keep getting stronger.",
                "Do NOT rush to add heavy weights right away. Progression can be achieved by: slowing down the tempo (3 seconds to lower down), adding 1-2 reps per set, or reducing rest by 5 seconds.",
                "Upgrade exercise variations: Incline Push-Up -> Knee Push-Up -> Standard Push-Up.",
                "Listen to your joints. If an exercise creates sharp pain, regress to the beginner variation immediately."
            ),
            contentHi = listOf(
                "प्रगतिशील अधिभार (Progressive Overload) का अर्थ है समय के साथ शरीर पर धीरे-धीरे चुनौती बढ़ाना।",
                "तुरंत भारी वजन उठाने की जल्दी न करें। आप गति को धीमा करके (जैसे 3 सेकंड में नीचे जाना) या 1-2 रैप्स बढ़ाकर भी प्रगति कर सकते हैं।",
                "कठिनाई का स्तर बदलें: दीवार पर पुश-अप -> घुटने पर पुश-अप -> सामान्य पुश-अप।",
                "अपने जोड़ों की सुनें। यदि कहीं तेज दर्द हो, तो तुरंत आसान संस्करण पर वापस आ जाएं।"
            )
        ),
        LearnArticle(
            id = "recovery_rest_days",
            titleEn = "The Science of Recovery & Rest Days",
            titleHi = "रिकवरी और आराम के दिनों का वैज्ञानिक महत्व",
            iconName = "Bedtime",
            summaryEn = "Why rest is the most productive part of your training program.",
            summaryHi = "आराम आपके पूरे फिटनेस सफर का सबसे महत्वपूर्ण हिस्सा क्यों है।",
            contentEn = listOf(
                "Workouts break down microscopic muscle fibers; sleep, nutrition, and rest days rebuild them thicker, stronger, and more resilient.",
                "Active Recovery: Light walking, gentle stretching, or playing with pets. Increases blood flow without causing fatigue.",
                "Aim for 7 to 9 hours of quality sleep each night. 95% of human growth hormone release occurs during deep sleep stages.",
                "Hydrate: Drink plenty of water and eat whole foods with protein and vegetables."
            ),
            contentHi = listOf(
                "वर्कआउट से मांसपेशियों के सूक्ष्म रेशे टूटते हैं; नींद और आराम के दिन ही वे फिर से मजबूत होकर जुड़ते हैं।",
                "सक्रिय रिकवरी (Active Recovery): हल्का टहलना, हल्की स्ट्रेचिंग जिससे शरीर थके बिना रक्त संचार बना रहे।",
                "हर रात 7 से 9 घंटे की अच्छी नींद लें। गहरी नींद में ही शरीर की मरम्मत होती है।",
                "भरपूर पानी पिएं और पौष्टिक आहार लें।"
            )
        )
    )
}
