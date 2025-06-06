
* Exhaustive Search on Gigabase
* x games
* noisy moves = pawn moves or captures = number of MagSignature comparisons
* unfiltered vs. MatSignature cut-off with various queries (opening, middle game, end game position)
* V1 vs. V2

|      | early cutoff | % | moves | % | noisy moves | % | time | % |
| ---- | ---: | ---- | ---: | ---- | ---: | ---- | ---: | ---- |
| unfiltered 16M |  |  | 1,339,365,575 |  | 546,438,342 |  | 365.3 |  |
| unfiltered 1M |  |  | 90,964,781 |      | 36,701,975 |      | 16.6 |      |
| Initial V1 |  |  | 1,155,227 | | 1,000,000 | | ~5.7 | |
| Initial V2 |  |  | " | | " | | ~3.8 |  |
| Opening V1      |  |  | 3,283,606 |      | 2,223,271 |      | 4.5 |      |
| Opening V2   | 362,446 | 36% | 2,203,130 |  | 1,482,764 |  | 6.4 |      |
| Middle-Game V1      |  |  | 7,075,610 |      | 4,365,071 |      | 4.4 |  |
| Middle-Game V2   | 745,071 | 75% | 1,027,954 |  | 664,827 |  | 4.6 |      |
| Middle-Game V1 |  |  | 12,378,320 |  | 6,862,357 |  | 4.8 | |
| Middle-Game V2 | 691,509 | 69% | 3,344,647 |  | 1,938,608 |  | 4.9 | |
| End-Game V1      |  |  | 51,957,757 |      | 23,662,933 |      | 8.9 |      |
| End-Game V2   | 916,224 | 91% | 4,213,126 |  | 1,981,611 |  | 4.9 |      |
| End-Game V1 |  |  | 73,670,521 |  | 32,304,269 |  | 11.3 | |
| End-Game V2 | 899,240 | 90% | 4,151,055 |  | 2,008,972 |  | 4.9 | |

* conclusion: MatSignature V2 improves selectivity over V1 in the middle and end game
* not consistently but not so bad
* backtracking in V2 is not a bottleneck but helpful; <= 16 steps; 2-3 on avg.
* early cut-off is very effective. particularly in the end-game. think about an udf !
