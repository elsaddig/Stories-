package com.example.data.model

object StoryData {
    val stories = listOf(
        Story(
            id = "story_1",
            title = "The Citadel's Joker",
            imageUrl = "https://images.unsplash.com/photo-1547483238-f400e65ccd56?w=600&auto=format&fit=crop&q=60", // castle library vibe
            summary = "Jack was a clever jester in an old kingdom. He used riddles and witty tricks to prevent wars and teach the furious king important lessons of fairness.",
            b1Text = "A long time ago, in a huge castle, lived a joker named Jack. Jack was very famous because he was energetic and funny. He was not a normal jester; he was extremely clever. Whenever the king made a bad decision, Jack would create a witty riddle. He used humor to explain important lessons. One afternoon, a fierce merchant arrived. He complained about the heavy taxes. The king was furious and wanted to put him in prison. Jack danced into the room and told a funny story about a busy bee who took too much honey from flowers and became too heavy to fly. The king laughed and understood Jack's subtle message. The king reduced the tax and released the merchant. Everyone praised the clever joker for saving a life.",
            b2Text = "Deep within the bastions of an ancient citadel, there resided a legendary joker named Jack. Far from a conventional court jester, Jack possessed boundless vivacity and profound intellect. He acted as an esteemed, subtle counselor. Whenever the monarch was on the verge of executing an arbitrary decree, Jack would weave a sophisticated riddle. His goal was to convey vital political wisdom through the veil of sharp witticism. One heavy afternoon, a robust and resentful merchant petitioned the court. He vehemently protested the oppressive levies. The monarch, consumed by rage, ordered his immediate incarceration. Jack gracefully bounded to the dais. He narrated an amusing allegory about an insatiable bee who accumulated excessive nectar until it was utterly immobilized. The monarch chuckled, instantly decoding the underlying prudence of Jack's parable. Jack's clever intervention successfully averted a political catastrophe.",
            b1MaskedWords = listOf("energetic", "clever", "furious", "subtle", "reduced"),
            b2MaskedWords = listOf("citadel", "vivacity", "arbitrary", "incarceration", "insatiable", "prudence"),
            b1Quiz = listOf(
                QuizQuestion(
                    id = 101,
                    question = "Why was Jack famous in the castle?",
                    options = listOf("He was strong and fought in wars", "He was energetic, funny, and clever", "He was a rich merchant", "He built the castle"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 102,
                    question = "What story did Jack tell to save the merchant?",
                    options = listOf("A story about a greedy king", "A story about a busy bee taking too much honey", "A story about a clown in another town", "A story about a fierce lion"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 103,
                    question = "What did the king do after Jack's story?",
                    options = listOf("He became even more furious", "He made Jack the new king", "He reduced the tax and released the merchant", "He banned honey in the castle"),
                    correctAnswerIndex = 2
                )
            ),
            b2Quiz = listOf(
                QuizQuestion(
                    id = 201,
                    question = "How is Jack's role described in the citadel?",
                    options = listOf("An ordinary entertainer with no real voice", "A conventional jester who made silly sounds", "An esteemed, subtle counselor who conveyed political wisdom", "A physical bodyguard of the monarch"),
                    correctAnswerIndex = 2
                ),
                QuizQuestion(
                    id = 202,
                    question = "What caused the merchant's potential incarceration?",
                    options = listOf("He stole gold from the citadel treasury", "He vehemently protested against oppressive levies", "He insulted the jester in public", "He arrived late to the monarch's speech"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 203,
                    question = "What is the meaning of Jack's 'insatiable bee' allegory?",
                    options = listOf("Greed and excess can immobilize and harm you", "Bees make valuable products for kingdoms", "Citadels need to cultivate more flower gardens", "Merchants should buy and sell honey"),
                    correctAnswerIndex = 0
                )
            )
        ),
        Story(
            id = "story_2",
            title = "The Echo of Wisdom",
            imageUrl = "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=600&auto=format&fit=crop&q=60", // book and candle vibe
            summary = "A young traveler discovers a smart speaker dictionary carved from a magic whispering tree that unlocks the deep bilingual secrets of vocabulary.",
            b1Text = "In a quiet valley, a young traveler named Leo found an old wooden box. Inside was a tiny speaker made of magic wood. When Leo whispered an English word, the speaker did not just say the meaning. It spoke the word in a soft voice, sang an example, and explained how it was used in ancient stories. 'This is a smart dictionary!' Leo told his friends. But the magic speaker had a secret. It only worked if Leo practiced learning five words every day. If he skipped a day, the speaker would sound quiet and dusty. Leo stayed determined to keep his daily streak. In a few months, he spoke English perfectly. He realized that the dynamic context of daily learning was the true magic.",
            b2Text = "Nestled within a serene valley, an intrepid traveler named Leo unearthed a carved timber artifact. Upon whispering any complex English term, the artifact did not merely compile static definitions. Instead, it articulated the word with precise phonetics, generated vivid contextual narratives, and provided dynamic Arabic translations. Realizing he had discovered an intelligent learning lexicon, Leo committed to exploring the lexical depths. However, this ancient repository operated under a strict covenant: it demanded daily diligence. If Leo failed to sustain his learning streak, the acoustic resonance would degrade into a dry murmur. Spurred by this gamified constraint, Leo cultivated unwavering consistency, transforming his passive vocabulary into elegant, fluent expression.",
            b1MaskedWords = listOf("traveler", "whispered", "determined", "streak", "context"),
            b2MaskedWords = listOf("serene", "intrepid", "lexicon", "covenant", "resonance", "consistency"),
            b1Quiz = listOf(
                QuizQuestion(
                    id = 301,
                    question = "What was special about the magical wooden box?",
                    options = listOf("It was full of shiny gold coins", "It acted as a smart speaker dictionary", "It played loud dance music", "It was a tiny clock"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 302,
                    question = "What happened if Leo skipped learning for a day?",
                    options = listOf("The box would explode", "Its sound became quiet and dusty", "It turned into a dry leaf", "It would call the village guards"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 303,
                    question = "How did Leo achieve perfection in his English speech?",
                    options = listOf("By moving to an English country", "By practicing five words every single day", "By reading only action newspapers", "By sleep learning"),
                    correctAnswerIndex = 1
                )
            ),
            b2Quiz = listOf(
                QuizQuestion(
                    id = 401,
                    question = "What was the artifact's function upon receiving a vocal input?",
                    options = listOf("It stored the word in cloud servers", "It articulated phonetics, contextual narratives, and bilingual translations", "It translated the text into ancient runes only", "It rejected the pronunciation of travelers"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 402,
                    question = "What 'covenant' bound the user of the learning lexicon?",
                    options = listOf("Paying gold coins to the valley keeper", "Sustaining a daily diligence streak to maintain acoustic resonance", "Never speaking words aloud in the citadel", "Only looking up nouns"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    id = 403,
                    question = "What does the prompt imply about Leo's outcome?",
                    options = listOf("He lost interest and buried the wooden artifact", "He turned his vocabulary into elegant, fluent expression through consistency", "The speaker became dusty and ceased performing", "He sold the device to castle counselors"),
                    correctAnswerIndex = 1
                )
            )
        )
    )
}
