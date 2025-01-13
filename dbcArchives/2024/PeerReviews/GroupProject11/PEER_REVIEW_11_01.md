# Peer-review of ScaDaMaLe Student Group Project Number 11

# Reviewer

Markus Fritzsche

# 1. Live/Video Presentation

Score: 2

Overall, I feel comfortable reimplementing your approach after watching your presentation.
However, I have one major complaint about the presentation: the lack of explanation for the symbols used in the formulas.
While I understand that most of the audience likely followed the ideas and notation given their strong background in AI, I had to spend some time deciphering the meaning of certain symbols on slide 4.

My interpretation of the symbols: 

$l$: current node

$C$: cost function

$x$: input features (matrix?)

$y$: target values (vector?)

$d$: number of features

$\tau$: threshold

$\sigma$: weight of importance?

$h$: the trained tree

$\pi$: a single path down the tree


# 2. Structure of Project Repository

Score: 2

If someone asked me where to find a particular part of the repository, I could easily pinpoint the file or directory without much thought.
Based on this, there is not much more I would ask for.
The only suggestion I can offer is to create a separate subfolder for the src/ files, but this is a minor complaint and mostly a matter of personal preference.


# 3. Comments and explanations in code:

Score: 1

If I were to go more in-depth, a common practice is to split very long functions into smaller ones by outsourcing parts of the function logic into private functions.

Example: 
Transform 
```
fn function1() {
    // very long code block
}
```
into 
```
fn function1() {
    sub_routine_1()
    //...
    subroutine_k()
}
```
By giving each sub_routine function a descriptive name, understanding function1 becomes much easier.

Take your main function as an example: it is almost 150 lines long, with few comments in between. If you introduced sub-functions for the ```args.estimator``` switch cases, the code would be more accessible to most readers.


# 4. Originality or Difficulty of the Project

Score: 2

#### Originality

The motivation behind the project is excellent.
Unlike some groups who used federated learning merely as a means to make their project parallel, your project aligns more closely with the goals of the course.

#### Difficulty

It’s hard for me to determine how difficult the project actually is.
I don’t recall precisely, but I believe the formula for missingness-avoidance is not new, and implementing a plain non-parallel version is likely straightforward.
However, optimizing the runtime of the code, including making changes to the Spark Scala sources, is far from trivial.

# 5. Scalability of the Project

Score: 2.

Your Scala plots show an almost constant training time.
Based on this, the scaling possibilities are quite impressive.

However, you are using artificial datasets, and in practice, it all comes down to how much data is available for your decision trees.
In my field, I don’t work with large-scale data, and I’m not sure if there is a market for MA decision trees, but that doesn’t mean there isn’t one.


# 6. Total Grade

The Total Grade is: 9




