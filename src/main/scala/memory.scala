import chisel3._
import chisel3.util.experimental.loadMemoryFromFileInline

//- start memory
class Memory() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W))
    val rdData = Output(UInt(8.W))
    val wrAddr = Input(UInt(10.W))
    val wrData = Input(UInt(8.W))
    val wrEna = Input(Bool())
  })

  val mem = SyncReadMem(1024, UInt(8.W))

  io.rdData := mem.read(io.rdAddr)

  when(io.wrEna) {
    mem.write(io.wrAddr, io.wrData)
  }
}
//- end

//- start memory_forwarding
class ForwardingMemory() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W))
    val rdData = Output(UInt(8.W))
    val wrAddr = Input(UInt(10.W))
    val wrData = Input(UInt(8.W))
    val wrEna = Input(Bool())
  })

  val mem = SyncReadMem(1024, UInt(8.W))

  val wrDataReg = RegNext(io.wrData)
  val doForwardReg = RegNext(io.wrAddr === io.rdAddr && io.wrEna)

  val memData = mem.read(io.rdAddr)

  when(io.wrEna) {
    mem.write(io.wrAddr, io.wrData)
  }

  io.rdData := Mux(doForwardReg, wrDataReg, memData)
}
//- end

object ForwardingMemory extends App {
  emitVerilog(new ForwardingMemory(), Array("--target-dir", "generated"))
  // emitVerilog(new TrueDualPortMemory(), Array("--target-dir", "generated", "--target:fpga"))
}

//- start memory_write_first
class MemoryWriteFirst() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W))
    val rdData = Output(UInt(8.W))
    val wrAddr = Input(UInt(10.W))
    val wrData = Input(UInt(8.W))
    val wrEna = Input(Bool())
  })

  //- start memory_write_first
  val mem = SyncReadMem(1024, UInt(8.W), SyncReadMem.WriteFirst)
  //- end

  io.rdData := mem.read(io.rdAddr)

  when(io.wrEna) {
    mem.write(io.wrAddr, io.wrData)
  }
}

class TrueDualPortMemory() extends Module {
  val io = IO(new Bundle {
    val addrA = Input(UInt(10.W))
    val rdDataA = Output(UInt(8.W))
    val wrEnaA = Input(Bool())
    val wrDataA = Input(UInt(8.W))
    val addrB = Input(UInt(10.W))
    val rdDataB = Output(UInt(8.W))
    val wrEnaB = Input(Bool())
    val wrDataB = Input(UInt(8.W))
  })

  //- start memory_dual_port
  // This dual-port memory generates registers (in Quartus with Cyclone V)
  val mem = SyncReadMem(1024, UInt(8.W))

  io.rdDataA := mem.read(io.addrA)
  when(io.wrEnaA) {
    mem.write(io.addrA, io.wrDataA)
  }
  io.rdDataB := mem.read(io.addrB)
  when(io.wrEnaB) {
    mem.write(io.addrB, io.wrDataB)
  }
  //- end
}

object TrueDualPortMemory extends App {
  emitVerilog(new TrueDualPortMemory(), Array("--target-dir", "generated"))
  // emitVerilog(new TrueDualPortMemory(), Array("--target-dir", "generated", "--target:fpga"))
}

class InitMemoryFile() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W))
    val rdData = Output(UInt(8.W))
    val wrAddr = Input(UInt(10.W))
    val wrData = Input(UInt(8.W))
    val wrEna = Input(Bool())
  })

  //- start memory_init_file
  val mem = SyncReadMem(1024, UInt(8.W))
  loadMemoryFromFileInline(
    mem, "./src/main/resources/init.hex", firrtl.annotations.MemoryLoadFileType.Hex
  )

  io.rdData := mem.read(io.rdAddr)
  // alternatively, io.rdData := mem(io.rdAddr)

  when(io.wrEna) {
    mem.write(io.wrAddr, io.wrData)
    // alternatively, mem(io.wrAddr) := io.wrData
  }
  //- end
}

class InitMemory() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W))
    val rdData = Output(UInt(8.W))
    val wrAddr = Input(UInt(10.W))
    val wrData = Input(UInt(8.W))
    val wrEna = Input(Bool())
  })

  //- start memory_init
  val hello = "Hello, World!"
  val helloHex = hello.map(_.toInt.toHexString).mkString("\n")
  val file = new java.io.PrintWriter("hello.hex")
  file.write(helloHex)
  file.close()

  val mem = SyncReadMem(1024, UInt(8.W))
  loadMemoryFromFileInline(mem, "hello.hex", firrtl.annotations.MemoryLoadFileType.Hex)
  //- end

  io.rdData := mem.read(io.rdAddr)
  when(io.wrEna) {
    mem.write(io.wrAddr, io.wrData)
  }
}
