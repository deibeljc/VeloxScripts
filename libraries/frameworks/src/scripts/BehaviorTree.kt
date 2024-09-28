package scripts.behaviortree

// Define the result types for node execution
enum class BehaviorTreeStatus {
  SUCCESS,
  FAILURE,
  RUNNING,
  KILL
}

// Interface for all nodes in the behavior tree
interface IBehaviorNode {
  val label: String?

  fun tick(): BehaviorTreeStatus

  val children: List<IBehaviorNode>
}

// Base class for parent nodes
abstract class IParentNode(override val label: String? = null) : IBehaviorNode {
  protected val _children = mutableListOf<IBehaviorNode>()
  override val children: List<IBehaviorNode>
    get() = _children

  fun addChild(node: IBehaviorNode) {
    _children.add(node)
  }
}

// Sequence node: succeeds if all children succeed
class SequenceNode(label: String? = null) : IParentNode(label) {
  override fun tick(): BehaviorTreeStatus {
    for (child in children) {
      val status = child.tick()
      if (status != BehaviorTreeStatus.SUCCESS) {
        return status
      }
    }
    return BehaviorTreeStatus.SUCCESS
  }
}

// Selector node: succeeds if any child succeeds
class SelectorNode(label: String? = null) : IParentNode(label) {
  override fun tick(): BehaviorTreeStatus {
    for (child in children) {
      val status = child.tick()
      if (status == BehaviorTreeStatus.SUCCESS) {
        return BehaviorTreeStatus.SUCCESS
      } else if (status == BehaviorTreeStatus.KILL) {
        return BehaviorTreeStatus.KILL
      }
    }
    return BehaviorTreeStatus.FAILURE
  }
}

// Condition node: evaluates a boolean condition
class ConditionNode(override val label: String? = null, private val condition: () -> Boolean) :
  IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()

  override fun tick(): BehaviorTreeStatus {
    return if (condition()) BehaviorTreeStatus.SUCCESS else BehaviorTreeStatus.FAILURE
  }
}

// Perform node: executes an action and always returns success
class PerformNode(override val label: String? = null, private val action: () -> Unit) :
  IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()

  override fun tick(): BehaviorTreeStatus {
    action()
    return BehaviorTreeStatus.SUCCESS
  }
}

// Conditional decorator node: runs child if condition is true
class ConditionalNode(private val condition: () -> Boolean, private val child: IBehaviorNode) :
  IBehaviorNode {
  override val label: String = "Conditional"
  override val children: List<IBehaviorNode> = listOf(child)

  override fun tick(): BehaviorTreeStatus {
    return if (condition()) {
      child.tick()
    } else {
      BehaviorTreeStatus.FAILURE
    }
  }
}

// RepeatUntil node: repeats child until a certain status is returned
class RepeatUntilNode(
  private val stopStatus: () -> Boolean,
  private val child: IBehaviorNode
) : IBehaviorNode {
  override val label: String = "RepeatUntil"
  override val children: List<IBehaviorNode> = listOf(child)

  override fun tick(): BehaviorTreeStatus {
    while (true) {
      val status = child.tick()
      if (stopStatus() || status == BehaviorTreeStatus.KILL) {
        return status
      }
    }
  }
}

// BehaviorTree class to build and execute the tree
class BehaviorTree(private val root: IBehaviorNode) {
  fun tick(): BehaviorTreeStatus {
    return root.tick()
  }
}

// Helper functions to build the tree using DSL-like syntax
fun behaviorTree(label: String? = null, init: IParentNode.() -> Unit): BehaviorTree {
  val root = SequenceNode(label)
  root.init()
  return BehaviorTree(root)
}

fun IParentNode.sequence(label: String? = null, init: IParentNode.() -> Unit) {
  val node = SequenceNode(label)
  node.init()
  addChild(node)
}

fun IParentNode.selector(label: String? = null, init: IParentNode.() -> Unit) {
  val node = SelectorNode(label)
  node.init()
  addChild(node)
}

fun IParentNode.condition(label: String? = null, predicate: () -> Boolean) {
  addChild(ConditionNode(label, predicate))
}

fun IParentNode.perform(label: String? = null, action: () -> Unit) {
  addChild(PerformNode(label, action))
}

fun IParentNode.conditional(initCondition: () -> Boolean, init: IParentNode.() -> Unit) {
  val childNode = SequenceNode()
  childNode.init()
  addChild(ConditionalNode(initCondition, childNode))
}

fun IParentNode.repeatUntil(stopStatus: () -> Boolean, init: IParentNode.() -> Unit) {
  val childNode = SequenceNode()
  childNode.init()
  addChild(RepeatUntilNode(stopStatus, childNode))
}
