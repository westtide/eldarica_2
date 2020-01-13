package lazabs.horn.concurrency
import java.io.{File, FileWriter, PrintWriter}

import ap.parser._
import lazabs.horn.abstractions.VerificationHints._
import lazabs.horn.bottomup.HornClauses
import lazabs.horn.preprocessor.HornPreprocessor.VerificationHints

import scala.collection.mutable.ListBuffer

class GraphTranslator(hornClauses : Seq[HornClauses.Clause],file:String) {

  import HornClauses.Clause

  //println(file.substring(file.lastIndexOf("/")+1))
  val fileName=file.substring(file.lastIndexOf("/")+1)
  //println(fileName)
  //val writer = new PrintWriter(new File("graphs/"+fileName+".gv"))
  val writer = new PrintWriter(new File("../graphs/"+fileName+".gv")) //python path

  // println(hornClauses)

  //println("digraph dag {")
  writer.write("digraph dag {"+"\n")

  val predicates =
    (HornClauses allPredicates hornClauses).toList sortBy (_.name)
  val predIndex =
    (for ((p, n) <- predicates.iterator.zipWithIndex) yield (p -> n)).toMap

  for (p <- predicates){
    //println("" + predIndex(p) + " [label=\"" + p.name + "\"];")
    writer.write("" + predIndex(p) + " [label=\"" + p.name + "\"];"+"\n")
  }

  for (Clause(IAtom(phead, _), body, _) <- hornClauses;
       if phead != HornClauses.FALSE;
       IAtom(pbody, _) <- body) {
    //println(predIndex(pbody) + " -> " + predIndex(phead))
    writer.write(predIndex(pbody) + " -> " + predIndex(phead)+"\n")
  }

  //println("}")
  writer.write("}"+"\n")
  writer.close()
}

class GraphTranslator_hint(hornClauses : Seq[HornClauses.Clause],
                           file:String,hints:VerificationHints,
                           InitialHintsWithID:Seq[wrappedHintWithID]) {
  var nodeCount:Int=0
  var root=new TreeNode
  var logString:String="" //store node information

  //create fileName.hints.graphs directory
  val fileName=file.substring(file.lastIndexOf("/")+1)
  //val pathName= "graphs/"+fileName+".hints.graphs"+"/"
  val pathName= "../graphs/"+fileName+".hints.graphs"+"/" //python path
  //val hintFileName=head.name.toString()+":"+oneHint.toString()+".gv"
  val hintFile = new File(pathName)
  hintFile.mkdir() //create fileName.hints.graphs directory

  println("---graph translator (hints)---")
  for(wrappedHint<-InitialHintsWithID) {

    val category=wrappedHint.hint.take(wrappedHint.hint.indexOf("("))

    //write graphviz form to .gv file
    val writer = new PrintWriter(new FileWriter(pathName+wrappedHint.ID.toString+".gv")) //create ID.gv file
//    writer.write(wrappedHint.head+":"+wrappedHint.hint+"\n")
//    writer.write("@@@"+"\n")
    writer.write("digraph dag {"+"\n")


    //root=new TreeNode
    var rootMark=root

    root.data=Map(nodeCount ->wrappedHint.head) //first node is template head
    //println(nodeCount + " [label=\""+ head.toString() +"\"];")
    logString=logString+(nodeCount + " [label=\""+ wrappedHint.head +"\"];"+"\n")
    nodeCount=nodeCount+1
    root.lchild = new TreeNode(Map(nodeCount->category.toString())) //second node is template category
    //println(nodeCount + " [label=\""+ category.toString() +"\"];")
    logString=logString+(nodeCount + " [label=\""+ category.toString() +"\"];"+"\n")
    nodeCount=nodeCount+1
    root=root.lchild



//      writer.write("digraph dag {"+"\n") //write some dummy content
//      writer.write("0 [label=\""+head+"\"];"+"\n") //root node is locaton/head
//      writer.write("1 [label=\""+category+"\"];"+"\n")//second node is hint's category
//      writer.write("0->1"+"\n")
//      writer.write("}"+"\n")
    for((head,hintList)<-hints.getPredicateHints()){
      for(oneHint<-hintList){
        if(head.name.toString==wrappedHint.head && oneHint.toString==wrappedHint.hint){
          translateHint(oneHint,root)
        }
      }
    }
    //translateHint(oneHint,root)
    nodeCount=0
    root=new TreeNode


    //println("Tree:")
    BinarySearchTree.preOrder(rootMark)
    logString=logString+BinarySearchTree.relationString
    BinarySearchTree.relationString=""

    writer.write(logString)
    writer.write("}"+"\n")
    logString=""

    writer.close()



  }

  def translateHint(h:VerifHintElement,root:TreeNode):Unit= h match{
    case VerifHintInitPred(p) => translateExpr(p,root)
    case VerifHintTplPred(p,_) => translateExpr(p,root)
    case VerifHintTplPredPosNeg(p,_) => translateExpr(p,root)
    case VerifHintTplEqTerm(t,_) => translateExpr(t,root)
    case VerifHintTplInEqTerm(t,_) => translateExpr(t,root)
    case VerifHintTplInEqTermPosNeg(t,_) => translateExpr(t,root)

  }

  def translateExpr(e:IExpression,root:TreeNode):Unit= {
    //println(e)

    e match{
      case IAtom(pred,args)=> println("IAtom");
      case IBinFormula(j,f1,f2)=>{
        if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->j.toString))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ j.toString +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(f1,root.lchild)
          translateExpr(f2,root.lchild)

        }else if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->j.toString))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ j.toString +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(f1,root.rchild)
          translateExpr(f2,root.rchild)
        }


      }
      case IBoolLit(value)=>{
        if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->(value.toString)))
          //root=root.lchild
        }else if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->(value.toString)))
          //root=root.rchild
        }
        //println(nodeCount + " [label=\""+ "_"+index +"\"];")
        logString=logString+(nodeCount + " [label=\""+value.toString() +"\"];"+"\n")
        nodeCount=nodeCount+1
      }
      case IConstant(c)=> {
        if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->(c.toString)))
          //root=root.lchild
        }else if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->(c.toString)))
          //root=root.rchild
        }

        //println(nodeCount + " [label=\""+ "_"+index +"\"];")
        logString=logString+(nodeCount + " [label=\""+c.toString() +"\"];"+"\n")
        nodeCount=nodeCount+1
      }
      case IEpsilon(cond)=> println("IEpsilon");
      //case IFormula()=>println("IFormula")
      case IFormulaITE(cond,left,right)=>println("IFormulaITE")
      case IFunApp(fun,args)=>println("IFunApp");
      case IIntFormula(rel,t)=> {
        if(root.lchild==null){
          if(rel.toString=="EqZero"){
            root.lchild = new TreeNode(Map(nodeCount->"="))
            //println(nodeCount + " [label=\""+ "=" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "=" +"\"];"+"\n")
            nodeCount=nodeCount+1
            root.lchild.rchild = new TreeNode(Map(nodeCount->"0"))
            //println(nodeCount + " [label=\""+ "0" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "0" +"\"];"+"\n")
            nodeCount=nodeCount+1
            //root=root.lchild
            translateExpr(t,root.lchild)

          }
          if(rel.toString=="GeqZero"){
            root.lchild = new TreeNode(Map(nodeCount->">="))
            logString=logString+(nodeCount + " [label=\""+ ">=" +"\"];"+"\n")
            nodeCount=nodeCount+1
            root.lchild.rchild = new TreeNode(Map(nodeCount->"0"))
            //println(nodeCount + " [label=\""+ "0" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "0" +"\"];"+"\n")
            nodeCount=nodeCount+1
            //root=root.lchild
            translateExpr(t,root.lchild)
            //println(nodeCount + " [label=\""+ ">=" +"\"];")

          }
        }else if(root.rchild==null){
          if(rel.toString=="EqZero"){
            root.rchild = new TreeNode(Map(nodeCount->"="))
            //println(nodeCount + " [label=\""+ "=" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "=" +"\"];"+"\n")
            nodeCount=nodeCount+1
            root.rchild.rchild = new TreeNode(Map(nodeCount->"0"))
            //println(nodeCount + " [label=\""+ "0" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "0" +"\"];"+"\n")
            nodeCount=nodeCount+1
            //root=root.lchild
            translateExpr(t,root.rchild)

          }
          if(rel.toString=="GeqZero"){
            root.rchild = new TreeNode(Map(nodeCount->">="))
            logString=logString+(nodeCount + " [label=\""+ ">=" +"\"];"+"\n")
            nodeCount=nodeCount+1
            root.rchild.rchild = new TreeNode(Map(nodeCount->"0"))
            //println(nodeCount + " [label=\""+ "0" +"\"];")
            logString=logString+(nodeCount + " [label=\""+ "0" +"\"];"+"\n")
            nodeCount=nodeCount+1
            //root=root.lchild
            translateExpr(t,root.rchild)
            //println(nodeCount + " [label=\""+ ">=" +"\"];")

          }
        }



      }
      case IIntLit(value)=>{
        if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->(value.toString)))
          //root=root.lchild
        }else if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->(value.toString)))
          //root=root.rchild
        }
        //println(nodeCount + " [label=\""+ "_"+index +"\"];")
        logString=logString+(nodeCount + " [label=\""+value.toString() +"\"];"+"\n")
        nodeCount=nodeCount+1
      }
      case INamedPart(name,subformula)=>println("INamedPart")
      case INot(subformula)=>{
        if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->"!"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "!" +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(subformula,root.lchild)

        }else if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->"!"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "!" +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(subformula,root.rchild)
        }
      }
      case IPlus(t1,t2)=> {
        if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->"+"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "+" +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(t1,root.lchild)
          translateExpr(t2,root.lchild)

        }else if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->"+"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "+" +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(t1,root.rchild)
          translateExpr(t2,root.rchild)

        }

      }
      case IQuantified(quan, subformula)=>{
        if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->quan.toString))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ quan.toString +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(subformula,root.lchild)

        }else if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->quan.toString))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "+" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ quan.toString +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(subformula,root.rchild)
        }
      }
      //case ITerm()=>println("ITerm")
      case ITermITE(cond,left,right)=>println("ITermITE")
      case ITimes(coeff,t)=> {
        if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->"*"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "*" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "*" +"\"];"+"\n")
          nodeCount=nodeCount+1
          root.lchild.rchild = new TreeNode(Map(nodeCount->coeff.toString()))
          //println(nodeCount + " [label=\""+ coeff +"\"];")
          logString=logString+(nodeCount + " [label=\""+ coeff +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(t,root.lchild)
        }else if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->"*"))
          //root=root.lchild
          //println(nodeCount + " [label=\""+ "*" +"\"];")
          logString=logString+(nodeCount + " [label=\""+ "*" +"\"];"+"\n")
          nodeCount=nodeCount+1
          root.rchild.rchild = new TreeNode(Map(nodeCount->coeff.toString()))
          //println(nodeCount + " [label=\""+ coeff +"\"];")
          logString=logString+(nodeCount + " [label=\""+ coeff +"\"];"+"\n")
          nodeCount=nodeCount+1
          translateExpr(t,root.rchild)
        }

      }
      case ITrigger(patterns,subformula)=>println("ITrigger");
      case IVariable(index)=> {
        if(root.rchild==null){
          root.rchild = new TreeNode(Map(nodeCount->("_"+index.toString)))
          //root=root.lchild
          logString=logString+(nodeCount + " [label=\""+ "_"+index +"\"];"+"\n")
          nodeCount=nodeCount+1
        }else if(root.lchild==null){
          root.lchild = new TreeNode(Map(nodeCount->("_"+index.toString)))
          //root=root.rchild
          logString=logString+(nodeCount + " [label=\""+ "_"+index +"\"];"+"\n")
          nodeCount=nodeCount+1
        }

      }

      case _=>println("?")
    }
//    for (subExpr <- e.subExpressions) {
//
//      translateExpr(subExpr)
//
//    }



  }
}

class TreeNode{
  var data:Map[Int,String]=Map(0->"new")
  //var data:Map[Int,String]=Map()
  var lchild:TreeNode = null//val
  var rchild:TreeNode = null
  var parent:TreeNode = null

  def this(data:Map[Int,String]){
    this()
    this.data = data
  }
}

object BinarySearchTree {

  var relationString:String="" //store node relation information
  def preOrder(root: TreeNode): Unit = {
    if (root != null) {

      //println(root.data)

      val (k,v) = root.data.head

      if(root.lchild!=null){
        val (l_key,l_value)=root.lchild.data.head
        //println(k+"->"+l_key)
        if(k!=l_key){
          relationString=relationString+(k+"->"+l_key+"\n")
        }

      }
      if(root.rchild!=null){
        val (r_key,r_value)=root.rchild.data.head
        //println(k+"->"+r_key)
        relationString=relationString+(k+"->"+r_key+"\n")
      }
      preOrder(root.lchild)
      //print(root.data.keys + "\n")
      preOrder(root.rchild)
      //print(root.data.keys + "\n")

    }

  }

  def inOrder(root: TreeNode): Unit = {
    if (root != null) {
      inOrder(root.lchild)
      println(root.data)
      inOrder(root.rchild)
    }
  }
}

class TreeNodeForGraph{
  var data:Map[String,String]=Map("0"->"new")
  //var data:Map[Int,String]=Map()
  var lchild:TreeNodeForGraph = null//val
  var rchild:TreeNodeForGraph = null
  var parent:TreeNodeForGraph = null

  def this(data:Map[String,String]){
    this()
    this.data = data
  }
}

  object BinarySearchTreeForGraph {
    var headArgList=ListBuffer[ArgumentNode]()
    var bodyArgList=ListBuffer[ArgumentNode]()
    var ASTtype=""
    var relationString:String="" //store node relation information
    var nodeString:String="" //store node information
    def preOrder(root: TreeNodeForGraph): Unit = {
      if (root != null) {
        //println(root.data)
        val (k,v) = root.data.head

        if(root.lchild!=null){
          val (l_key,l_value)=root.lchild.data.head
          //println(k+"->"+l_key)
//          if(k!=l_key && !bodyArgList.isEmpty){
//            if(headArgList.isEmpty){//no data flow needed
//              relationString=relationString+(l_key+"->"+k+"\n")
//            }else{
//              var tempFlag=false
//
//              for(arg<-bodyArgList if tempFlag==false){
//                if(arg.originalContent==l_value){
//                  //println(arg.name+"->"+k+"\n")
//                  relationString=relationString+(arg.name+"->"+k+"\n") //use argument to replace original node
//                  deleteANode(l_key)//delete original node
//                  tempFlag=true
//
//                }else{
//                  //println(l_key+"->"+k+"\n")
//                }
//              }
//              if(tempFlag==false){
//                relationString=relationString+(l_key+"->"+k+"\n")
//              }
//
//            }
//
//          }
//          if(k!=l_key && bodyArgList.isEmpty){
//            relationString=relationString+(l_key+"->"+k+"\n")
//          }
          if(k!=l_key){
            relationString=relationString+(l_key+"->"+k+"\n")
          }
        }

        if(root.rchild!=null){
          val (r_key,r_value)=root.rchild.data.head
          //println(k+"->"+r_key)
//          if(k!=r_key && !bodyArgList.isEmpty){
//            if(headArgList.isEmpty){//no data flow needed
//              relationString=relationString+(r_key+"->"+k+"\n")
//            }else{
//              var tempFlag=false
//              for(arg<-bodyArgList if tempFlag==false){
//                if(arg.originalContent==r_value){
//                  //println(arg.name+"->"+k+"\n")
//                  relationString=relationString+(arg.name+"->"+k+"\n") //use argument to replace original node
//                  deleteANode(r_key)//delete original node
//                  tempFlag=true
//                }else{
//                  //println(r_key+"->"+k+"\n")
//                }
//              }
//              if(tempFlag==false){
//                relationString=relationString+(r_key+"->"+k+"\n")
//              }
//
//            }
//          }
//          if(k!=r_key && bodyArgList.isEmpty){
//            relationString=relationString+(r_key+"->"+k+"\n")
//          }
          if(k!=r_key){
            relationString=relationString+(r_key+"->"+k+"\n")
          }

        }

        preOrder(root.lchild)
        //print(root.data.keys + "\n")
        preOrder(root.rchild)
        //print(root.data.keys + "\n")

      }

    }


    def deleteANode(node:String): Unit ={
      var tempRelation=""
      var sList:Array[String]=nodeString.split("\n")
      var deleteList=ListBuffer[String]()
      for(line<-sList if line.contains(node)){
        deleteList+=line
      }
      var nodeList=ListBuffer[String]()
      for(line<-sList){
        nodeList+=line
      }
      for(line<-deleteList){
        nodeList-=line
      }
      //sList.filter(! _.contains(line))
      for(line<-nodeList){
        tempRelation=tempRelation+line
      }
      nodeString=tempRelation
    }

}




class ArgumentNode(headName:String,bodyName:String,location:String,clauseID:Int,arg:String,argIndex:Int) {
  val InList=ListBuffer
  val OutList=ListBuffer
  val index=argIndex
  val name=location+"_argument_"+index.toString
  var argumentEdgeFlag=false
  val originalContent=arg
  var constantFlowInNode:String=""
  val dataFLowHyperEdge=new DataFlowHyperEdge(bodyName,headName,name,clauseID)
}

class ControlFowHyperEdge(body:String,head:String,ind:Int) {
  val from=body
  val to=head
  val index=ind
  val name="ControlFowHyperEdge_"+ind.toString//index is clauses's ID

}
class DataFlowHyperEdge(body:String,head:String,guardedArgument:String,ind:Int) {
  val from=body
  val to=head
  val name="DataFowHyperEdge_"+ind.toString+"_"+guardedArgument

}

class ControlFlowNode(nodeName:String,argumentNodeList:ListBuffer[ArgumentNode]){
  val name=nodeName
  var argumentList:ListBuffer[ArgumentNode]=argumentNodeList

  def getArgNameByContent(c:String): String ={
    var name=""
    for(arg<-argumentList){
      if(arg.originalContent==c){
        name=arg.name
      }
    }
    name
  }

}

class ClauseTransitionInformation(controlFlowHead:ControlFlowNode,controlFLowBody:ControlFlowNode,id:Int
                                  //,controlFlowHyperEdge:ControlFowHyperEdge,dataFlowHyperedge:ListBuffer[DataFowHyperEdge]
                                 ){
  //head node
  val head=controlFlowHead
  val body=controlFLowBody
  val clauseID=id
  var controlFlowHyperEdge=new ControlFowHyperEdge(body.name,head.name,clauseID)
  var dataFlowHyperEdgeList=ListBuffer[DataFlowHyperEdge]()
  var guardNumber=0
  var dataFlowNumber=0
  var guardASTGraph=Map[String,String]()//rootName->graph
  var dataFlowASTGraph=ListBuffer[DataFlowASTGraphInfo]()
  var simpleDataFlowConnection=Map[String,String]()//map:hyperedge->connectiongraph
  val name:String=head.name+"___"+body.name
  var guardASTRootName=""
  //var dataFlowASTRootNameList=ListBuffer[String]()
  val trueCondition="true_"+id.toString
  var commonArg=ListBuffer[ArgumentNode]()
  //body node
}

class DataFlowASTGraphInfo(rootName:String,argName:String,graphInfo:String){
  val astRootName=rootName
  val argumentName=argName
  val graphText=graphInfo

}