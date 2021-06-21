(set-logic HORN)
(set-info :source |
    Benchmark: C_VC
    Output by Princess (http://www.philipp.ruemmer.org/princess.shtml)
|)
(set-info :status unknown)
(declare-heap Heap Addr HeapObject
 defObj
 ((HeapObject 0)) (
  (
   (O_Int (getInt Int))
   (O_Addr (getAddr Addr))
   (defObj)
  )
))
(declare-fun inv_main11 (Heap Addr Addr Addr Addr Int) Bool)
(declare-fun inv_main12 (Heap Addr Addr Addr Addr Int) Bool)
(declare-fun inv_main13 (Heap Addr Addr Addr Addr Int Int) Bool)
(declare-fun inv_main14 (Heap Addr Addr Int) Bool)
(declare-fun inv_main2 (Heap) Bool)
(declare-fun inv_main3 (Heap Addr) Bool)
(declare-fun inv_main5 (Heap Addr Addr) Bool)
(declare-fun inv_main7 (Heap Addr Addr) Bool)
(declare-fun inv_main8 (Heap Addr Addr Addr Addr) Bool)
(assert (inv_main2 (as emptyHeap Heap)))
(assert (forall ((var0 Addr) (var1 Addr) (var2 Heap) (var3 Heap) (var4 Addr) (var5 Addr)) (or (not (and (inv_main5 var2 var4 var5) (and (and (= var3 (write var2 var5 (O_Int 42))) (= var1 var4)) (= var0 var5)))) (inv_main8 var3 var1 var0 var1 var0))))
(assert (forall ((var0 Addr) (var1 Int) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Addr) (var6 Int)) (or (not (inv_main13 var2 var3 var5 var4 var0 var1 var6)) (inv_main12 (write var2 var4 (O_Int var6)) var3 var5 var4 var0 var1))))
(assert (forall ((var0 Addr) (var1 Addr) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Int) (var6 Heap) (var7 Addr) (var8 Addr) (var9 Addr) (var10 Addr)) (or (not (and (inv_main12 var6 var8 var10 var9 var4 var5) (and (and (and (and (= var2 (write var6 var4 (O_Int var5))) (= var7 var8)) (= var3 var10)) (= var0 var9)) (= var1 var4)))) (inv_main7 var2 var7 var3))))
(assert (forall ((var0 Addr) (var1 Heap) (var2 Addr) (var3 Addr) (var4 Addr)) (or (not (inv_main8 var1 var2 var4 var3 var0)) (inv_main11 var1 var2 var4 var3 var0 (getInt (read var1 var3))))))
(assert (forall ((var0 Heap)) (or (not (inv_main2 var0)) (inv_main3 (newHeap (alloc var0 (O_Int 0))) (newAddr (alloc var0 (O_Int 0)))))))
(assert (forall ((var0 Addr) (var1 Heap) (var2 Heap) (var3 Addr)) (or (not (and (inv_main3 var1 var3) (and (= var2 (write var1 var3 (O_Int 3))) (= var0 var3)))) (inv_main5 (newHeap (alloc var2 (O_Int 0))) var0 (newAddr (alloc var2 (O_Int 0)))))))
(assert (forall ((var0 Addr) (var1 Int) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Addr)) (or (not (inv_main11 var2 var3 var5 var4 var0 var1)) (inv_main13 var2 var3 var5 var4 var0 var1 (getInt (read var2 var0))))))
(assert (forall ((var0 Heap) (var1 Addr) (var2 Addr)) (or (not (inv_main7 var0 var1 var2)) (inv_main14 var0 var1 var2 (getInt (read var0 var1))))))
(assert (forall ((var0 Heap) (var1 Addr)) (not (and (inv_main3 var0 var1) (not (is-O_Int (read var0 var1)))))))
(assert (forall ((var0 Heap) (var1 Addr) (var2 Addr)) (not (and (inv_main5 var0 var1 var2) (not (is-O_Int (read var0 var2)))))))
(assert (forall ((var0 Addr) (var1 Heap) (var2 Addr) (var3 Addr) (var4 Addr)) (not (and (inv_main8 var1 var2 var4 var3 var0) (not (is-O_Int (read var1 var3)))))))
(assert (forall ((var0 Addr) (var1 Int) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Addr)) (not (and (inv_main11 var2 var3 var5 var4 var0 var1) (not (is-O_Int (read var2 var0)))))))
(assert (forall ((var0 Addr) (var1 Int) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Addr) (var6 Int)) (not (and (inv_main13 var2 var3 var5 var4 var0 var1 var6) (not (is-O_Int (read var2 var4)))))))
(assert (forall ((var0 Addr) (var1 Int) (var2 Heap) (var3 Addr) (var4 Addr) (var5 Addr)) (not (and (inv_main12 var2 var3 var5 var4 var0 var1) (not (is-O_Int (read var2 var0)))))))
(assert (forall ((var0 Heap) (var1 Addr) (var2 Addr)) (not (and (inv_main7 var0 var1 var2) (not (is-O_Int (read var0 var1)))))))
(assert (forall ((var0 Int) (var1 Heap) (var2 Addr) (var3 Addr)) (not (and (inv_main14 var1 var2 var3 var0) (not (= var0 42))))))
(check-sat)