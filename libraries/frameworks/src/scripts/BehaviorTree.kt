package scripts.frameworks

// Define the result types for node execution
enum class BehaviorTreeStatus {
  SUCCESS,
  FAILURE,
  RUNNING,
  KILL,
  IDLE // New status
}

// Interface for all nodes in the behavior tree
interface IBehaviorNode {
  val label: String?
  val children: List<IBehaviorNode>

  var status: BehaviorTreeStatus // Changed to var

  fun tick()

  // Add a method to reset the node status
  fun reset()
}

// Base class for parent nodes
abstract class IParentNode(override val label: String? = null) : IBehaviorNode {
  protected val _children = mutableListOf<IBehaviorNode>()
  override val children: List<IBehaviorNode>
    get() = _children

  // Change initial status to IDLE
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE

  // Implement reset method
  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    _children.forEach { it.reset() }
  }

  fun addChild(node: IBehaviorNode) {
    _children.add(node)
  }
}

// Sequence node: succeeds if all children succeed
class SequenceNode(label: String? = null) : IParentNode(label) {
  private var currentChildIndex = 0

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING

    while (currentChildIndex < children.size) {
      val child = children[currentChildIndex]
      child.tick()
      when (child.status) {
        BehaviorTreeStatus.SUCCESS -> {
          currentChildIndex++
        }

        BehaviorTreeStatus.RUNNING -> {
          return
        }

        BehaviorTreeStatus.FAILURE, BehaviorTreeStatus.KILL -> {
          status = child.status
          resetRemainingChildrenFromIndex(currentChildIndex + 1)
          currentChildIndex = 0
          return
        }

        else -> {
          status = BehaviorTreeStatus.FAILURE
          return
        }
      }
    }

    // All children succeeded
    status = BehaviorTreeStatus.SUCCESS
    currentChildIndex = 0
  }

  private fun resetRemainingChildrenFromIndex(startIndex: Int) {
    for (i in startIndex until children.size) {
      children[i].reset()
    }
  }

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    currentChildIndex = 0
    _children.forEach { it.reset() }
  }
}


// Selector node: succeeds if any child succeeds
class SelectorNode(label: String? = null) : IParentNode(label) {
  private var currentChildIndex = 0

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING

    while (currentChildIndex < children.size) {
      val child = children[currentChildIndex]
      child.tick()
      when (child.status) {
        BehaviorTreeStatus.SUCCESS -> {
          status = BehaviorTreeStatus.SUCCESS
          resetRemainingChildrenFromIndex(currentChildIndex + 1)
          currentChildIndex = 0
          return
        }

        BehaviorTreeStatus.RUNNING -> {
          return
        }

        BehaviorTreeStatus.FAILURE -> {
          currentChildIndex++
        }

        BehaviorTreeStatus.KILL -> {
          status = BehaviorTreeStatus.KILL
          resetRemainingChildrenFromIndex(currentChildIndex + 1)
          currentChildIndex = 0
          return
        }

        else -> {
          status = BehaviorTreeStatus.FAILURE
          return
        }
      }
    }

    // All children failed
    status = BehaviorTreeStatus.FAILURE
    currentChildIndex = 0
  }

  private fun resetRemainingChildrenFromIndex(startIndex: Int) {
    for (i in startIndex until children.size) {
      children[i].reset()
    }
  }

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    currentChildIndex = 0
    _children.forEach { it.reset() }
  }
}


// Condition node: evaluates a boolean condition
class ConditionNode(override val label: String? = null, private val condition: () -> Boolean) :
  IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
  }

  override fun tick() {
    status = if (condition()) BehaviorTreeStatus.SUCCESS else BehaviorTreeStatus.FAILURE
  }
}


// Perform node: executes an action and always returns success
class PerformNode(override val label: String? = null, private val action: () -> Unit) :
  IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
  }

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    action()
    status = BehaviorTreeStatus.SUCCESS
  }
}


// Conditional decorator node: runs child if condition is true
class ConditionalNode(private val condition: () -> Boolean, private val child: IBehaviorNode) :
  IBehaviorNode {
  override val label: String = "Conditional"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    child.reset()
  }

  override fun tick() {
    if (condition()) {
      child.tick()
      status = child.status
    } else {
      status = BehaviorTreeStatus.FAILURE
      child.reset()
    }
  }
}

// RepeatUntil node: repeats child until a certain condition is met
class RepeatUntilNode(private val stopCondition: () -> Boolean, private val child: IBehaviorNode) :
  IBehaviorNode {
  override val label: String = "RepeatUntil"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    child.reset()
  }

  override fun tick() {
    if (stopCondition()) {
      status = BehaviorTreeStatus.SUCCESS
      child.reset()
      return
    }

    child.tick()

    when (child.status) {
      BehaviorTreeStatus.SUCCESS, BehaviorTreeStatus.FAILURE -> {
        child.reset()
        status = BehaviorTreeStatus.RUNNING
      }

      BehaviorTreeStatus.RUNNING -> {
        status = BehaviorTreeStatus.RUNNING
      }

      BehaviorTreeStatus.KILL -> {
        status = BehaviorTreeStatus.KILL
      }

      else -> {
        status = BehaviorTreeStatus.FAILURE
      }
    }
  }
}


// BehaviorTree class to build and execute the tree
class BehaviorTree(private val rootNode: IBehaviorNode) {
  fun tick() {
    rootNode.tick()
  }

  fun root(): IBehaviorNode {
    return rootNode
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

fun IParentNode.repeatUntil(stopCondition: () -> Boolean, init: IParentNode.() -> Unit) {
  val childNode = SequenceNode()
  childNode.init()
  addChild(RepeatUntilNode(stopCondition, childNode))
}
