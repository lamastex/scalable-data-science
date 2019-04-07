package org.lamastex.ex01

object ListReduce extends App {

  /**
   * This method computes the sum of all elements in the list xs. There are
   * multiple techniques that can be used for implementing this method, and
   * you will learn them in the sequel.
   *
   * For this exercise you can use the following methods in class
   * `List`:
   *
   *  - `xs.isEmpty: Boolean` returns `true` if the list `xs` is empty
   *  - `xs.head: Int` returns the head element of the list `xs`. If the list
   *    is empty an exception is thrown
   *  - `xs.tail: List[Int]` returns the tail of the list `xs`, i.e. the the
   *    list `xs` without its `head` element
   *
   *  ''Hint:'' instead of writing a `for` or `while` loop, think of `reduce`.
   *
   * @param xs A list of natural numbers
   * @return The sum of all elements in `xs`
   */
    def sum(xs: List[Int]): Int = xs.reduce((x,y) => x+y)
  
  /**
   * This method returns the largest element in a list of integers. If the
   * list `xs` is empty it throws a `java.util.NoSuchElementException`.
   *
   * You can use the same methods of the class `List` as mentioned above.
   *
   * ''Hint:'' Again, think of `reduce` instead of looping. You may have 
   * to first define an function to return maximum of two Int's
   *
   * @param xs A list of natural numbers
   * @return The largest element in `xs`
   * @throws java.util.NoSuchElementException if `xs` is an empty list
   */
    def max(xs: List[Int]): Int = xs.reduce((x,y) => {if(x>y) x else y})
  
  /**
   * This method returns the smallest element in a list of integers. If the
   * list `xs` is empty it throws a `java.util.NoSuchElementException`.
   *
   * You can use the same methods of the class `List` as mentioned above.
   *
   * ''Hint:'' Again, think of `reduce` instead of looping.
   *
   * @param xs A list of natural numbers
   * @return The smallest element in `xs`
   * @throws java.util.NoSuchElementException if `xs` is an empty list
   */
    println("please fix one character in min(xs) to get the right minimum")
    def min(xs: List[Int]): Int = xs.reduce((x,y) => {if(x>y) x else y})

    val aList = List(42,24,3,6,9,-3,26,9)
    val aListSum = sum(aList)
    val aListMax = max(aList)
    val aListMin = min(aList)
    println(s"For the List = ${aList}\n its sum, max and min respectively are:\n ${aListSum}, ${aListMax}, and ${aListMin}")
  }
