
* Exhaustive Search on Gigabase
* x games
* noisy moves = pawn moves or captures = number of MagSignature comparisons
* unfiltered vs. MatSignature cut-off with various queries (opening, middle game, end game position)
* V1 vs. V2

|      | moves | % | noisy moves | % | time | % |
| ---- | ---: | ---- | ---: | ---- | ---: | ---- |
| unfiltered 16M | 1,339,365,575 |  | 546,438,342 |  | 365.3 |  |
| unfiltered 1M | 90,964,781 |      | 36,701,975 |      | 16.6 |      |
| Initial V1 | 1,155,227 | | 1,000,000 | | ~5.7 | |
| Initial V2 | " | | " | | ~3.8 |  |
| Opening V1      | 3,283,606 |      | 2,223,271 |      | 4.5 |      |
| Opening V2   | 3,158,123 | 96% | 2,164,862 | 97% | 4.2 |      |
| Middle-Game V1      | 7,075,610 |      | 4,365,071 |      | 4.4 |  |
| Middle-Game V2   | 3,953,082 | **56%** | 2,582,159 | **59%** | 4.5 |      |
| Middle-Game V1 | 12,378,320 |  | 6,862,357 |  | 4.8 | |
| Middle-Game V2 | 10,486,043 | 85% | 6,085,174 | 89% | 5.4 | |
| End-Game V1      | 51,957,757 |      | 23,662,933 |      | 8.9 |      |
| End-Game V2   | 44,839,231 | 86% | 21,005,142 | 88% | 9.6 |      |
| End-Game V1 | 73,670,521 |  | 32,304,269 |  | 11.3 | |
| End-Game V2 | 39,198,539 | **53%** | 18,753,813 | **58%** | 10.3 | |

* conclusion: MatSignature V2 improves selectivity over V1 in the middle and end game
* not consistently but not so bad
* backtracking in V2 is not a bottleneck but helpful; <= 16 steps; 2-3 on avg.
* times remain more or less equal. well...
