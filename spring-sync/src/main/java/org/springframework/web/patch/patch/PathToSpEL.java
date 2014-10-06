/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.patch.patch;

import java.util.Arrays;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

class PathToSpEL {

	private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

	public static Expression pathToExpression(String path) {
		return SPEL_EXPRESSION_PARSER.parseExpression(pathToSpEL(path));
	}
	
	public static Expression spelToExpression(String spel) {
		return SPEL_EXPRESSION_PARSER.parseExpression(spel);
	}	
	
	public static String pathToSpEL(String path) {
		return pathNodesToSpEL(path.split("\\/"));
	}
	
	public static String pathToParentSpEL(String path) {
		return pathNodesToSpEL(Arrays.copyOf(path.split("\\/"), path.split("\\/").length - 1));
	}

	public static String pathNodesToSpEL(String[] pathNodes) {
		StringBuilder spelBuilder = new StringBuilder();
		
		for(int i=0; i < pathNodes.length; i++) {
			String pathNode = pathNodes[i];
			
			if (pathNode.length() == 0) {
				continue;
			}
			try {
				int index = Integer.parseInt(pathNode);
				spelBuilder.append('[').append(index).append(']');
			} catch (NumberFormatException e) {
				if (spelBuilder.length() > 0) {
					spelBuilder.append('.');	
				}
				spelBuilder.append(pathNode);
			}
		}
		
		String spel = spelBuilder.toString();
		if (spel.length() == 0) {
			spel = "#this";
		}
		return spel;		
	}

}
