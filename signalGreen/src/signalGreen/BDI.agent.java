
//only works for one type of reasoning.
// algo is not complete at all yet

/*basic algo is:
	have currrent set of intentions
	have current set of beliefs
	while true:
		get new input from enviroment
		update beliefs
		update options
		update intentions
		create plan
	carry out plan 


	you would pass me some state like nearby/slow car
	I would recommend a set of plans*/





private class BDIReasoning {

	//percepts are what the agent perceives about the enviroment, currently
	Queue<Percept> currentPercepts = new Queue<Percept>().add(Percept slowRoad)


/* Beliefs are what the agent thinks the actual enviroment state is like
  there often updated with new percepts */

List<Beliefs> currentBeliefs = new LinkedList<Beliefs>().add(fasterIsBetter).add(roadsAreQuiet);



//Intentions are what the agent goals are. Go to Vertex A, using route a or b..., slow down, speed up etc
	PrioriortyQueue<Intentions> currentIntentions = new PriorityQueue<Intention>().add(AtoB);

///options are potential intentions
	List<Options> options = new LinkedList<Options>();


	BDIReasonining(vehicle,enum type){
		while true{
			deliberate();
	}


	//new state about enviroment has been received
	Percept p = this.currentPercept
 

	public addPercept(Percept p)
		this.currentPercepts.addFirst(p)

	public getListOfIntentions/Plans?


	private void deliberate(){
		beliefRevisionFunction(this.currentBeliefs,this.currentPercepts);
		getOptions()
		filter();



	
	





	
	//beliefs represent internal state, so update
	private void 

	//work out how important, add to list
	//new list of beliefs asessed, removed
	//maybe have a confidence attribute for each belief?
	//e.g. if one belief is road A is quiet...lower the confidence
	//for that belief
	private void beliefRevisionFunction(Belief,p)();



		//based on possibly new beliefs, new intentions
		//so generate new options
	private getOptions(){
		List<Intention> intents = this.currentIntentions;
		LinkedList<Desires> = new LinkedList<Desires>()
		//again, maybe chuck all the confidence numbers and
		//see how they compare against how prioritised the
		//Itentions are, and return ordered list?

		//like IntentionNo1 is higher, but beliefs are weak
		//so IntentionNo2 gets ordered first

		return List<Option>(goSlower, goFaster, sameSpeed);


		


	private void filter(){
		//if sum of beliefs say roads are busy
		//best intention is to slow down for now
		return null;}








//to share the intentions with you, I could either have a  getter
//or just  use threads and shared memory, pretty sure all the Qeueue
//classes are thread safe anyway


//Ive spent all day trying to get windows installed,
//Im just gonna use the uni lab from now on
//I really think I should do this, as I will code from now on
//and I might as well put to use what I learnt last semester...


		
		








		
		

