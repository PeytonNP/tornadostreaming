/**
 * Copyright Jul 5, 2015
 * Author : Ahmed Mahmood
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.tornado.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.purdue.cs.tornado.helper.IndexCellCoordinates;
import edu.purdue.cs.tornado.helper.Point;
import edu.purdue.cs.tornado.helper.Rectangle;
import edu.purdue.cs.tornado.helper.SpatioTextualConstants;
import edu.purdue.cs.tornado.messages.Query;

public class GlobalGridIndex {
	private Double xrange;
	private Double yrange;
	private Double xStep;
	private Double yStep;
	private Integer xCellsNum;
	private Integer yCellsNum;
	private HashMap<Integer, Integer> taskIdToIndex ;
	private Integer numberOfEvaluatorTasks;
	private List<Integer> evaluatorBoltTasks; //this keeps track of the evaluator bolts ids 
	public Integer getNumberOfEvaluatorTasks() {
		return numberOfEvaluatorTasks;
	}
	public void setNumberOfEvaluatorTasks(Integer numberOfEvaluatorTasks) {
		this.numberOfEvaluatorTasks = numberOfEvaluatorTasks;
	}
	public List<Integer> getEvaluatorBoltTasks() {
		return evaluatorBoltTasks;
	}
	public void setEvaluatorBoltTasks(List<Integer> evaluatorBoltTasks) {
		this.evaluatorBoltTasks = evaluatorBoltTasks;
	}
	public HashMap<Integer, Integer> getTaskIdToIndex() {
		return taskIdToIndex;
	}
	public void setTaskIdToIndex(HashMap<Integer, Integer> taskIdToIndex) {
		this.taskIdToIndex = taskIdToIndex;
	}
	public GlobalGridIndex(Integer numberOfEvaluatorTasks,List<Integer> evaluatorBoltTasks){
		this.numberOfEvaluatorTasks = numberOfEvaluatorTasks;
		this.evaluatorBoltTasks = evaluatorBoltTasks;
		taskIdToIndex = new HashMap<Integer, Integer>();
		for(Integer i =0;i<numberOfEvaluatorTasks;i++){
			taskIdToIndex.put(evaluatorBoltTasks.get(i), i);
		}
		
		xrange = SpatioTextualConstants.xMaxRange;
		yrange = SpatioTextualConstants.yMaxRange;
		yCellsNum = xCellsNum = SpatioTextualConstants.globalGridGranularity;//(int)Math.sqrt(numberOfEvaluatorTasks);
		xStep = xrange/xCellsNum;
		yStep = yrange/yCellsNum;
	}
	public Rectangle getBoundsForTaskId(Integer taskId){
		Integer taskIndex = taskIdToIndex.get(taskId);
		return getBoundsForTaskIndex(taskIndex);
	}
	public Rectangle getBoundsForTaskIndex(Integer taskIndex){
		IndexCellCoordinates globalIndexSelfcoordinates = new IndexCellCoordinates(taskIndex/xCellsNum,taskIndex%xCellsNum);
		Point minPoint = new Point ();
		minPoint.setX(xStep*globalIndexSelfcoordinates.getX());
		minPoint.setY(yStep*globalIndexSelfcoordinates.getY());
		Point maxPoint = new Point ();
		maxPoint.setX(xStep*(globalIndexSelfcoordinates.getX()+1));
		maxPoint.setY(yStep*(globalIndexSelfcoordinates.getY()+1));
		return new Rectangle(minPoint, maxPoint);
	}
	public ArrayList<Integer> getTaskIDsOverlappingRecangle(Rectangle rectangle){
		return mapRecangleToEvaluatorTask(rectangle);
	}
	public ArrayList<Integer> getTaskIDsContainingPoint(Point point){
		
		return  mapDataPointToEvaluatorTask(point.getX(), point.getY());
		
	}
	public Double getXrange() {
		return xrange;
	}
	public void setXrange(Double xrange) {
		this.xrange = xrange;
	}
	public Double getYrange() {
		return yrange;
	}
	public void setYrange(Double yrange) {
		this.yrange = yrange;
	}
	public Double getxStep() {
		return xStep;
	}
	public void setxStep(Double xStep) {
		this.xStep = xStep;
	}
	public Double getyStep() {
		return yStep;
	}
	public void setyStep(Double yStep) {
		this.yStep = yStep;
	}
	public Integer getxCellsNum() {
		return xCellsNum;
	}
	public void setxCellsNum(Integer xCellsNum) {
		this.xCellsNum = xCellsNum;
	}
	public Integer getyCellsNum() {
		return yCellsNum;
	}
	public void setyCellsNum(Integer yCellsNum) {
		this.yCellsNum = yCellsNum;
	}
	public ArrayList<IndexCellCoordinates> mapDataPointToIndexCellCoordinates(Point point){
		ArrayList<IndexCellCoordinates> partitions = new ArrayList<IndexCellCoordinates>();
		Integer xCell = (int) (point.getX() / xStep);
		Integer yCell = (int) (point.getY() / yStep);
		if(xCell>=SpatioTextualConstants.xMaxRange/xStep)
			xCell=(int) ((SpatioTextualConstants.xMaxRange/xStep)-1);
		if(yCell>=SpatioTextualConstants.yMaxRange/xStep)
			yCell=(int) ((SpatioTextualConstants.yMaxRange/yStep)-1);
		if(xCell<0)
			xCell=0;
		if(yCell<0)
			yCell=0;
		partitions.add(new IndexCellCoordinates(xCell, yCell));
		return partitions;
	}
	public Integer mapIndexCellCoordinatedToTaskId(IndexCellCoordinates indexCellCoordinates){
		return evaluatorBoltTasks.get( indexCellCoordinates.getX() * yCellsNum + indexCellCoordinates.getY() );
	}
	private ArrayList<Integer> mapDataPointToEvaluatorTask(Double x, Double y) {
		ArrayList<Integer> partitions = new ArrayList<Integer>();
		Integer xCell = (int) (x / xStep);
		Integer yCell = (int) (y / yStep);
		if(xCell>=SpatioTextualConstants.xMaxRange/xStep)
			xCell=(int) ((SpatioTextualConstants.xMaxRange/xStep)-1);
		if(yCell>=SpatioTextualConstants.yMaxRange/xStep)
			yCell=(int) ((SpatioTextualConstants.yMaxRange/yStep)-1);
		if(xCell<0)
			xCell=0;
		if(yCell<0)
			yCell=0;
		
		Integer partitionNum = xCell * yCellsNum + yCell;
		
		if (partitionNum >= evaluatorBoltTasks.size()) {
			System.out.println("error in data " + x + " , " + y + "  index is "
					+ partitionNum + " while partitions "
					+ evaluatorBoltTasks.size());
		
		} else {
			// System.out.println("Point "+x+" , "+y+" is mapped to xcell:"+xCell+"ycell:"+yCell+" index is "+			 partitionNum+" to partitions "+evaluatorBoltTasks.get(partitionNum));
			 partitions.add( evaluatorBoltTasks.get(partitionNum));
		}
		return partitions;
	}
	private ArrayList<Integer> mapRecangleToEvaluatorTask(Rectangle rectangle) {
		ArrayList<Integer> partitions = new ArrayList<Integer>();
		int xMinCell = (int) (rectangle.getMin().getX() / xStep);
		int yMinCell = (int) (rectangle.getMin().getY() / yStep);
		int xMaxCell = (int) (rectangle.getMax().getX() / xStep);
		int yMaxCell = (int) (rectangle.getMax().getY() / yStep);
		//to handle the case where data is outside the range of the bolts 
		if(xMaxCell>=SpatioTextualConstants.xMaxRange/xStep)
			xMaxCell=(int) ((SpatioTextualConstants.xMaxRange/xStep)-1);
		if(yMaxCell>=SpatioTextualConstants.yMaxRange/xStep)
			yMaxCell=(int) ((SpatioTextualConstants.yMaxRange/yStep)-1);
		if(xMinCell<0)
			xMinCell=0;
		if(yMinCell<0)
			yMinCell=0;
		for (Integer xCell = xMinCell; xCell <= xMaxCell; xCell++)
			for (Integer yCell = yMinCell; yCell <= yMaxCell; yCell++) {
				Integer partitionNum = xCell * yCellsNum + yCell;
				if (partitionNum >= evaluatorBoltTasks.size())
					System.out.println("error in rectangle " + rectangle.getMin().getX() + " , " + rectangle.getMin().getY()
							+ " , " + rectangle.getMax().getX() + " , " + rectangle.getMax().getY() + "  index is "
							+ partitionNum + " while partitions "
							+ evaluatorBoltTasks.size());
				else {
					// System.out.println("Query "+xmin+" , "+ymin+" , "+xmax+" , "+ymax+" is mapped to xcell:"+xCell+"ycell:"+yCell+" index is "+
					// partitionNum+" to partitions "+_targets.get(partitionNum));
					partitions.add(evaluatorBoltTasks.get(partitionNum));
				}

			}
		
		return partitions;
	}
	public GlobalIndexKNNIterator globalKNNIterator(Query q){
		return new GlobalGridIndexIterator(this,q.getFocalPoint());
	}

}
