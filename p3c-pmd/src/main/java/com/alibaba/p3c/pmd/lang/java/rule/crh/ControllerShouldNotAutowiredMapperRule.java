package com.alibaba.p3c.pmd.lang.java.rule.crh;

import java.util.List;

import org.jaxen.JaxenException;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

/**
 * 检查Controller类中是否直接注入了xxxMapper或者XXXDao
 * 
 * @author yejg
 */
public class ControllerShouldNotAutowiredMapperRule extends AbstractAliRule {

	private static final String ANNOTATION_CONTROLLER = "Controller";
	private static final String ANNOTATION_REST_CONTROLLER = "RestController";

	private static final String ANNOTATION_AUTOWIRED = "Autowired";
	private static final String ANNOTATION_RESOURCE = "Resource";

	private static final String MAPPER = "mapper";
	private static final String DAO = "dao";

	@Override
	public Object visit(ASTTypeDeclaration node, Object data) {
		if (this.hasControllerAnnotation(node)) {
			try {
				List<Node> nodes = node.findChildNodesWithXPath("ClassOrInterfaceDeclaration/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration");
				if (nodes != null && nodes.size() > 0) {
					for (int i = 0; i < nodes.size(); i++) {
						ASTClassOrInterfaceBodyDeclaration tmpNode = (ASTClassOrInterfaceBodyDeclaration) (nodes.get(i));
						if (this.hasAutowiredAnnotion(tmpNode)) {
							List<ASTFieldDeclaration> astFieldDeclarations = tmpNode.findChildrenOfType(ASTFieldDeclaration.class);
							for (ASTFieldDeclaration astFieldDeclaration : astFieldDeclarations) {
								List<ASTType> childrenOfType = astFieldDeclaration.findChildrenOfType(ASTType.class);
								for (ASTType astType : childrenOfType) {
									String typeImage = astType.getTypeImage();
									if (typeImage != null && (typeImage.toLowerCase().endsWith(MAPPER) || typeImage.toLowerCase().endsWith(DAO))) {
										// super.addViolation(data, astType);
										ViolationUtils.addViolationWithPrecisePosition(this, astType, data,
								                I18nResources.getMessage("java.crh.ControllerShouldNotAutowiredMapperRule.rule.msg"));
									}
								}
							}
						}
					}
				}
			} catch (JaxenException e) {
				e.printStackTrace();
			}
		}
		return super.visit((JavaNode) node, data);
	}

	/**
	 * 判断ASTTypeDeclaration是否标记有Controller或RestController注解
	 *
	 * @param node
	 * @return 如果有Controller注解，则返回true
	 */
	private boolean hasControllerAnnotation(ASTTypeDeclaration node) {
		List<ASTAnnotation> childrenOfASTAnnotation = node.findChildrenOfType(ASTAnnotation.class);
		for (ASTAnnotation astAnnotation : childrenOfASTAnnotation) {
			String annotationName = astAnnotation.jjtGetChild(0).jjtGetChild(0).getImage();
			if (ANNOTATION_CONTROLLER.equals(annotationName) || ANNOTATION_REST_CONTROLLER.equals(annotationName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断ClassOrInterfaceBodyDeclaration是否包含Autowired、Resource注解
	 *
	 * @param bodyDeclaration
	 * @return 如果有注入的注解，返回true
	 */
	private boolean hasAutowiredAnnotion(ASTClassOrInterfaceBodyDeclaration bodyDeclaration) {
		List<ASTAnnotation> childrenOfType = bodyDeclaration.findChildrenOfType(ASTAnnotation.class);
		for (ASTAnnotation astAnnotation : childrenOfType) {
			// String annotationName = astAnnotation.getAnnotationName();// 高版本才有这个getAnnotationName方法
			String annotationName = astAnnotation.jjtGetChild(0).jjtGetChild(0).getImage();
			if (ANNOTATION_AUTOWIRED.equals(annotationName) || ANNOTATION_RESOURCE.equals(annotationName)) {
				return true;
			}
		}
		return false;
	}
}
