# Proposal: Fix rare race condition in the Fuzzy flow with legacy SecondString

## Intent
The root:
  java.lang.NullPointerException
      at java.util.TreeMap.rotateLeft(TreeMap.java:2221)
      at java.util.TreeMap.put(TreeMap.java:580)
      at com.wcohen.secondstring.tokens.SimpleTokenizer.intern(SimpleTokenizer.java:79)

SimpleTokenizer.intern mutates a TreeMap to dedupe token strings. TreeMap is not thread-safe, and NPE in rotateLeft during a put is the textbook signature of a
  concurrent-modification race corrupting the red-black tree's pointers.

  Where it bites in your harness: FuzzyTestingPipeline.scala:693-725:

  private final case class LegacyScorer(className: String) extends ((String, String) => Double) {
    @transient private lazy val instanceAndMethod: (AnyRef, Method) = {
      ...
      val instance = algorithmClass.getDeclaredConstructor().newInstance()
      (instance, scoreMethod)
    }

    override def apply(left: String, right: String): Double = {
      val (instance, scoreMethod) = instanceAndMethod
      scoreMethod.invoke(instance, left, right).asInstanceOf[Double]
    }
  }

  You hold one com.wcohen.secondstring.Jaccard instance per executor JVM (the lazy val). Spark executors run multiple tasks concurrently on the same JVM, both calling the
  UDF, both reaching into the same Jaccard → same SimpleTokenizer → same TreeMap.put. Eventually two tasks rebalance the tree at the same moment and one of them dereferences
  a null child.

## Scope
- Fix legacy flow

## What Changes: Fix rare race condition in the Fuzzy flow with legacy SecondString
Fix rare race condition in fuzzy testing flow.

## Approach
ThreadLocal scorer -- Each task thread gets its own SecondString instance, no contention. Mirrors the ThreadLocal pattern that is already used across the code.
