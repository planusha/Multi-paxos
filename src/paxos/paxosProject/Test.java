package paxosProject;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.util.ArrayList;
import java.util.HashMap;

import paxosProject.network.*;
import paxosProject.network.messages.*;
public class Test {
	NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT,  0);
	//NodeIdentifier serverID = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,  0);
	//NodeIdentifier proposerID = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,  0);

	public static void main(String []args) throws Exception {
		Test test = new Test();
		test.simpleTest(args[0]);
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();

		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		Configuration.addNodeAddress(clientID, new InetSocketAddress("localhost", 2002));
		//Configuration.addNodeAddress(serverID, new InetSocketAddress("localhost", 5002));
		//Configuration.addNodeAddress(proposerID, new InetSocketAddress("localhost", 4002));
		ArrayList<NodeIdentifier> proposers = new ArrayList<>();
		ArrayList<Proposer> prop = new ArrayList<>();
		ArrayList<NodeIdentifier> acceptors = new ArrayList<>();
		ArrayList<NodeIdentifier> learners = new ArrayList<>();
		for(Integer i: Configuration.proposerIDs.keySet()){
			NodeIdentifier id = Configuration.proposerIDs.get(i);
			Proposer ts = new Proposer(id);
			proposers.add(id);
			if(i ==1){
				ts.set_failLeader();
			}
			prop.add(ts);
		}
		for(Integer i: Configuration.acceptorIDs.keySet()){
			NodeIdentifier id = Configuration.acceptorIDs.get(i);
			Acceptor ts = new Acceptor(id);
			acceptors.add(id);
		}
		for(Integer i: Configuration.learnerIDs.keySet()){
			NodeIdentifier id = Configuration.learnerIDs.get(i);
			Learner ts = new Learner(id);
			//ts.setFail_send_to_client();
			learners.add(id);
		}
		for(Proposer p : prop) {
			p.startHeartBeat();
		}
		Client client = new Client(Configuration.clientIDs.get(1));
		HashMap<Integer,Integer> request1 = new HashMap<>();
		request1.put(567,200);
		//Configuration.electLeader();
		HashMap<Integer,Integer> request2 = new HashMap<>();

		request2.put(203,290);
		client.sendValue(request1);
		Thread.sleep(2000);
		//Configuration.electLeader();
		client.sendValue(request2);
	}
	
	class TestClient implements EventHandler {

		NodeIdentifier myID = null;
		Network network = null;
		public TestClient(NodeIdentifier myID){
			this.myID = myID;
			network = new NettyNetwork(myID, this);
		}

		public void sendValue(NodeIdentifier receiver, int index, HashMap hs){
			System.out.printf("server send %s => %s\n", new Put(myID,index, hs), receiver);
			network.sendMessage(receiver, new Put(myID,index, hs));
		}

		@Override
		public void handleMessage(Message msg) {
			System.out.println("handle message" + msg);
			if (msg instanceof Response) {
				System.out.printf("%s receive %s\n", myID, (Response) msg);
			}
		}

		@Override
		public void handleTimer() {


		}

		@Override
		public void handleFailure(NodeIdentifier node, Throwable cause) {
			if (cause instanceof ClosedChannelException) {
				System.out.printf("%s handleFailure get %s\n", myID, cause);
			}
		}
	}

//	class TestServer implements EventHandler {
//		NodeIdentifier myID = null;
//		Network network = null;
//		public TestServer(NodeIdentifier myID){
//			this.myID = myID;
//			network = new NettyNetwork(myID, this);
//		}
//
//		public void sendValue(NodeIdentifier receiver, int value){
//			System.out.printf("\nserver send %s => %s\n", new Accepted(myID, true), receiver);
//			network.sendMessage(receiver, new Accepted(myID, true));
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//
//			if (msg instanceof Accepted){
//				System.out.printf("\n%s receive %s\n", myID, (Accepted) msg);
//			}else if(msg instanceof Put){
//
//				System.out.printf("\n%s receive %s\n", myID, (Put) msg);
//			}else if(msg instanceof Get){
//				System.out.printf("\n%s receive %s\n", myID, (Get) msg);
//			} else if (msg instanceof Prepare){
//				System.out.printf("\n%s receive %s\n", myID, (Prepare) msg);
//			}else if(msg instanceof Promise){
//				System.out.printf("\n%s receive %s\n", myID, (Promise) msg);
//			}else if(msg instanceof Response){
//				System.out.printf("\n%s receive %s\n", myID, (Response) msg);
//			}else{
//				System.exit(1);
//			}
//
//		}
//
//		@Override
//		public void handleTimer() {
//		}
//
//		@Override
//		public void handleFailure(NodeIdentifier node, Throwable cause) {
//			if (cause instanceof ClosedChannelException) {
//				System.out.printf("%s handleFailure get %s\n", myID, cause);
//			}
//		}
//
//	}
}
