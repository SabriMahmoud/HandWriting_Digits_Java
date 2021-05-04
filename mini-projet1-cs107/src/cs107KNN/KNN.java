package cs107KNN;
import java.nio.ByteBuffer;
import java.sql.Struct;
import java.util.ArrayList;
public class KNN {
	private static final int byte_Image_Index=16 ; 
	private static final int byte_Labels_Index=8; 
	private  static final int magicImage=2051 ;
	private static final int magiclabels=2049 ;

	public static void main(String[] args) {
		byte b1 = 40; // 00101000
		byte b2 = 20; // 00010100
		byte b3 = 10; // 00001010
		byte b4 = 5; // 00000101
       
		// [00101000 | 00010100 | 00001010 | 00000101] = 672401925
		int result = extractInt(b1, b2, b3, b4);
		System.out.println(result);
		
        
	
  
		String bits = "10000001";
		System.out.println("La séquence de bits " + bits + "\n\tinterprétée comme byte non signé donne "
				+ Helpers.interpretUnsigned(bits) + "\n\tinterpretée comme byte signé donne "
				+ Helpers.interpretSigned(bits));
	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {
		byte [] B=new byte[]{b31ToB24,b23ToB16,b15ToB8,b7ToB0};
		int num = ByteBuffer.wrap(B).getInt();
		return(num);
	}

	/**
	 * Parses an IDX file containing images
	 *
	 * @param data the binary content of the file
	 *
	 * @return A tensor of images
	 */
	public static byte[][][] parseIDXimages(byte[] data) {
		if(extractInt(data[0],data[1],data[2],data[3])!=magicImage) return null ;
		else{

			int noImages=extractInt(data[4],data[5],data[6],data[7]) ;
			int height=extractInt(data[8],data[9],data[10],data[11]) ;
			int width=extractInt(data[12],data[13],data[14],data[15]);

			byte[][][] tensor=new byte[noImages][height][width];

			int index=byte_Image_Index;
			int imageIndex=0;
	
			while(index<data.length){
				//each double for iterations is an image 
				for(int i=0 ;i<height;i++){
					for(int j=0 ; j<width;j++){

						//scaling the data between -128 ,127  
						//data[index+j] starts from index=16

						int pixel_Value= data[index+j];
						tensor[imageIndex][i][j]=(byte)((pixel_Value&0xff)-128);
					
						
					}
					index+=width;
				}
				imageIndex++;
			}
			return tensor ;
		}
	  
	}

	/**
	 * Parses an idx images containing labels
	 *
	 * @param data the binary content of the file
	 *
	 * @return the parsed labels
	 */
	public static byte[] parseIDXlabels(byte[] data) {
		if(extractInt(data[0],data[1],data[2],data[3])!= magiclabels) return null;

		else{

			int noLabels=extractInt(data[4],data[5],data[6],data[7]);

			byte[] parsed_Labels= new byte[noLabels];
	

			for(int i=0 ; i<noLabels;i++){
		        //considering every byte is a label
				parsed_Labels[i]=data[i+byte_Labels_Index] ;

			}
			return(parsed_Labels) ; 
			

		}
		
	}

	/**
	 * @brief Computes the squared L2 distance of two images
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the squared euclidean distance between the two images
	 */
	public static float squaredEuclideanDistance(byte[][] a, byte[][] b) {
		float squared_distance=0 ;
		//i suppose that the two matrix has the same size height*width (test size in main function)
		for(int i=0 ; i<a.length ;i++){
			for(int j=0 ;j<a[0].length;j++){

				squared_distance+=Math.pow((byte)(a[i][j]-b[i][j]),2) ;

			}
		}
		return(squared_distance);
		
	}

/************** Process for inverted Similarity ***************/
/**************  ***************/
/**************  ***************/
/**  Pixel average function */
public static float pixelsAverage(byte[][] a){
	float sum=0 ;

	for(int i=0 ;i<a.length ;i++ ){
		for(int j=0 ;j<a[0].length ;j++ ){

			sum+=(float)a[i][j] ;

		}

	}

	return(sum/(a.length*a[0].length)) ;
}

/*Denominateur du SI(A,B)*/ // verifier la formule 
public static float pixelScaling(byte[][]a){

	float sum=0 ; 
	float average=pixelsAverage(a) ;

	for(int i=0 ; i<a.length;i++){
		for(int j=0;j<a[0].length ;j++){
            
			sum+=Math.pow((a[i][j]-average),2) ;

		}
	}
	return(sum) ;

}

/*Denominator Si */ 
public static float si_Denominator(byte[][]a,byte[][]b){
	return (float)Math.sqrt(pixelScaling(a)*pixelScaling(b)) ;

}

 /*Nominateur du si*/
 public static float si_nominator(byte[][]a,byte[][]b){
    float nominator =0;
	float b_average=pixelsAverage(b) ;
	float a_average=pixelsAverage(a) ;
	for(int i=0;i<a.length;i++){
		for(int j=0;j<a[0].length ;j++){
			nominator+=(a[i][j]-a_average)*(b[i][j]-b_average) ;
		}
	}
	return(nominator) ;
	

}



/**************  ***************/
/**************  ***************/
/************** End Process  inverted Similarity ***************/







	/**
	 * @brief Computes the inverted similarity between 2 images.
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the inverted similarity between the two images
	 */
	public static float invertedSimilarity(byte[][] a, byte[][] b) {
		if(si_Denominator(a, b)==0){
			return 2;
		}
		else return 1- (si_nominator(a, b)/si_Denominator(a, b));
		
	}


	/************** Process for QuickSort***************/
/**************  ***************/
/**************  ***************/
public static int partition(float a[],int[] indices, int low, int high)
{
	// Pick the rightmost element as a pivot from the array
	float pivot = a[high];

	// elements less than the pivot will go to the left of `pIndex`
	// elements more than the pivot will go to the right of `pIndex`
	// equal elements can go either way
	int indexPivot = low;

	// each time we find an element less than or equal to the pivot,
	// `pIndex` is incremented, and that element would be placed
	// before the pivot.
	
	for (int i = low; i <high; i++)
	{
		if (a[i] <= pivot)
		{
			swap(i, indexPivot,a,indices);
			indexPivot++;
		}
	}

	
	swap (indexPivot,high,a,indices);

	
	return indexPivot;
}


public static void quickProcess(float[] values, int[] indices,float pivot, int l, int h){
	
	while(l<h){
		if(values[l]<pivot){
			l++;
			System.out.println("Indices triés: L " );
		}
		else if (values[h]>pivot){
			h--;
			System.out.println("Indices triés: H" );
		}
		else {
			 l++;
			 h--;
			 swap(h, l, values, indices);
			 System.out.println("Indices triés: H L" );
	    }
	}	
}



/**************  ***************/
/**************  ***************/
/************** End Process  Quick sort ***************/


	/**
	 * @brief Quicksorts and returns the new indices of each value.
	 * 
	 * @param values the values whose indices have to be sorted in non decreasing
	 *               order
	 * 
	 * @return the array of sorted indices
	 * 
	 *         Example: values = quicksortIndices([3, 7, 0, 9]) gives [2, 0, 1, 3]
	 */


	public static int [] quicksortIndices(float[] values)
    {
        // create a stack for storing subarray start and end index
     
        ArrayList<Integer> list=new ArrayList<Integer>();
        // get the starting and ending index of the given array
        int low = 0;
        int high = values.length - 1;
 
        // push the start and end index of the array into the stack
        list.add(low) ;
        list.add(high);
 
        // loop till stack is empty
        int [] indices=new int[values.length];	
		for(int i=0;i<=values.length-1;i++){
			indices[i]=i;
		}
        while (!list.isEmpty())
        {
            // remove top pair from the list and get subarray starting
            // and ending indices
            low = list.get(0);
            high = list.get(1);
            list.clear();
 
            // rearrange elements across pivot
            int pivot = partition(values,indices,low,high);
 
            // push subarray indices containing elements that are
            // less than the current pivot to stack
            if (pivot - 1 > low) {
                list.add(low);
                list.add(pivot-1);
            }
 
            // push subarray indices containing elements that are
            // more than the current pivot to stack
            if (pivot + 1 < high) {
                list.add(pivot+1);
                list.add(high);
            }
        }
        return(indices) ;
    }
	/**
	 * @brief Sorts the provided values between two indices while applying the same
	 *        transformations to the array of indices
	 * 
	 * @param values  the values to sort
	 * @param indices the indices to sort according to the corresponding values
	 * @param         low, high are the **inclusive** bounds of the portion of array
	 *                to sort
	 */


	public static int[] quicksortIndices(float[] values, int[] indices, int low, int high) {
		float pivot=values[low] ;
		int l=low;
		int h=high;
		while(l<h){
				if(values[l]<pivot){
					l++;
					
				}
				else if (values[h]>pivot){
					h--;
					
				}
				else {
				 l++;
				 h--;
				 swap(h, l, values, indices);
				 
				}
								
}
		if (low<l) {
			quicksortIndices(values,indices, l, high);
		}
		if (high>h){
			quicksortIndices(values,indices,low, h);
		}
		return indices;
		
	}

	/**
	 * @brief Swaps the elements of the given arrays at the provided positions
	 * 
	 * @param         i, j the indices of the elements to swap
	 * @param values  the array floats whose values are to be swapped
	 * @param indices the array of ints whose values are to be swapped
	 */
	public static void swap(int i, int j, float[] values, int[] indices) {
		float aux=values[i];
		values[i]=values[j];
		values[j]=aux;
		int aux2=indices[i] ;
		indices[i]=indices[j] ;
		indices[j]=aux2;
	}

	/***************************Process Election ******************** */
	/***************************Process Election ******************** */
	/***************************Process Election ******************** */
	/***************************Process Election ******************** */


	public static ArrayList<Integer> whoCamesFirst(int [] voteTab,int [] whoIsFirst, int maxIndex,int k){
		ArrayList<Integer>valuesSupK=new ArrayList<Integer>();
		int same=voteTab[maxIndex];
		//condition : when voteTab[maxIndex] changes we get out of the while loop we need only the max values
		while(voteTab[maxIndex]==same){
			
			//constucting the structure list=[number max acc,index relative to sorted indices] 
			if(voteTab[maxIndex]>=k){
				valuesSupK.add(maxIndex);		
				valuesSupK.add(whoIsFirst[maxIndex]);
				voteTab[maxIndex]=-1 ;
			}

			else k-- ;
			
			maxIndex=indexOfMax(voteTab);
			
		}
		return valuesSupK ;



	}
	//Searching between the max values accuracy who came  fist in the sorted indices tab
	//knowing that the list valuesSupK contain this format [number max acc,index relative to sorted indices]
	public static int searchingMinIndex(ArrayList<Integer>valuesSupK){
		 
		int min=valuesSupK.get(1);
		for(int i=3 ;i<valuesSupK.size();i++){

			if(i%2 !=0 && min>valuesSupK.get(i)){	min=valuesSupK.get(i);	}

		}
		return min ;

	}
	/***************************End Process Election ******************** */
	/***************************End Process Election ******************** */
	/***************************End Process Election ******************** */
	/***************************End Process Election ******************** */
	

	/**
	 * @brief Returns the index of the largest element in the array
	 * 
	 * @param array an array of integers
	 * 
	 * @return the index of the largest integer
	 */
	public static int indexOfMax(int[] array) {
		int max=array[0] ;
		int indexMax=0;
		for(int i=1 ;i<array.length;i++){

			if(array[i]>max){	
				indexMax=i;
				max=array[i] ;
			}

		}
		return indexMax;
	}
    
	/**
	 * The k first elements of the provided array vote for a label
	 *
	 * @param sortedIndices the indices sorted by non-decreasing distance
	 * @param labels        the labels corresponding to the indices
	 * @param k             the number of labels asked to vote
	 *
	 * @return the winner of the election
	 */
	public static byte electLabel(int[] sortedIndices, byte[] labels, int k) {
		
		int [] voteTab=new int []{0,0,0,0,0,0,0,0,0,0};
		int [] whoIsFirt=new int []{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		for(int i=0;i<sortedIndices.length;i++){
			//Counting the accuracy of labels and storing it in the table voteTab
			voteTab[labels[sortedIndices[i]]]++;

			if (whoIsFirt[labels[sortedIndices[i]]]==-1 ){
				//Saving the relative first index for the label
				whoIsFirt[labels[sortedIndices[i]]]=sortedIndices[i];
			}
		}
        
		
		//Getting out the accuracy from vote tab in case of repetition

		ArrayList<Integer>valuesSupK=new ArrayList<Integer>();
		int maxIndex=indexOfMax(voteTab);
		
		//constucting the structure list=[number max acc,index relative to sorted indices] 
		valuesSupK=whoCamesFirst(voteTab,whoIsFirt,maxIndex,k); 
		

		//Searching between the max values accuracy who came  fist in the sorted indices tab
		//knowing that the list valuesSupK contain this format [number max acc,index relative to sorted indices] 
		
		int min=searchingMinIndex(valuesSupK);
	    //cleaning storage 
		valuesSupK.clear();
		return(labels[min])	;
	}

	/**
	 * Classifies the symbol drawn on the provided image
	 *
	 * @param image       the image to classify
	 * @param trainImages the tensor of training images
	 * @param trainLabels the list of labels corresponding to the training images
	 * @param k           the number of voters in the election process
	 *
	 * @return the label of the image
	 */
	public static byte knnClassify(byte[][] image, byte[][][] trainImages, byte[] trainLabels, int k) {
		//Distances Tab betwen  image and trainImages *
		float [] distancesTab=new float[trainImages.length];

		for(int i=0;i<trainImages.length;i++){
			
			distancesTab[i]=squaredEuclideanDistance(image,trainImages[i]);
		}
	
		
		int [] sortedIndices=new int[trainImages.length] ;
		sortedIndices=quicksortIndices(distancesTab);
		System.out.println(sortedIndices.length);
		byte labelElected=electLabel(sortedIndices, trainLabels,k);
		return labelElected;
	}

	/**
	 * Computes accuracy between two arrays of predictions
	 * 
	 * @param predictedLabels the array of labels predicted by the algorithm
	 * @param trueLabels      the array of true labels
	 * 
	 * @return the accuracy of the predictions. Its value is in [0, 1]
	 */
	public static double accuracy(byte[] predictedLabels, byte[] trueLabels) {
		double sumTrue=0 ;
		for(int i=0 ;i<predictedLabels.length;i++){
			if(predictedLabels[i]==trueLabels[i]){
				sumTrue++;
				System.out.println(sumTrue) ;
			}
		}
		return (sumTrue/predictedLabels.length);
	}
}
