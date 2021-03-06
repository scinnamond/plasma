/**
 *         PlasmaSDO™ License
 * 
 * This is a community release of PlasmaSDO™, a dual-license 
 * Service Data Object (SDO) 2.1 implementation. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. PlasmaSDO™ was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://plasma-sdo.org/licenses/>.
 *  
 */
package org.plasma.query.xpath;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LogicalExpr;
import org.jaxen.expr.RelationalExpr;
import org.plasma.query.model.RelationalOperator;
import org.plasma.query.model.Expression;
import org.plasma.query.model.Where;
import org.plasma.sdo.xpath.XPathExprVisitor;

/**
 * Receives Jaxen XPATH expression events as
 * the expression parse tree is traversed.  
 */
public class QueryPredicateVisitor implements XPathExprVisitor {

    private static Log log = LogFactory.getFactory().getInstance(QueryPredicateVisitor.class);
	private Where result;
	private Map<Expr, Expression> map = new HashMap<Expr, Expression>();
	
	public void visit(Expr target, Expr source, int level) {
		//log.info(target.getClass().getSimpleName() 
		//		+ " expr: " + target.toString());
		
		if (target instanceof BinaryExpr) {
			BinaryExpr binExpr = (BinaryExpr)target;
			Expression left = map.get(binExpr.getLHS());
			Expression right = map.get(binExpr.getRHS());
			if (left == null && right == null) {
				if (binExpr instanceof RelationalExpr) {
					RelationalExpr expr = (RelationalExpr)target;
					Expression qexpr = Expression.valueOf(expr);
					map.put(expr, qexpr);
				}
				else if (binExpr instanceof LogicalExpr) {
					LogicalExpr expr = (LogicalExpr)target;
					Expression qexpr = Expression.valueOf(expr);
					map.put(expr, qexpr);
				}
				else if (binExpr instanceof EqualityExpr) {
					EqualityExpr expr = (EqualityExpr)target;
					Expression qexpr = Expression.valueOf(expr);
					map.put(expr, qexpr);
				}
			}
			else {
				Expression qexpr = new Expression(
					left, 
					RelationalOperator.valueOf(binExpr.getOperator()), 
					right);
				map.put(binExpr, qexpr);				
			}
		}
		//else
		//	log.warn("could not process expression of type, "
		//			+ target.getClass().getSimpleName());
		
		if (source == null) { // root
			result = new Where();
			Expression qexpr = map.get(target);
			result.addExpression(qexpr);
		}
	}

	public Where getResult() {
		return result;
	}						
	
	
	
}
