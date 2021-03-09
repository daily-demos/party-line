import styled from "styled-components";
import theme from "../../theme";

export const SmallText = styled.p`
  font-size: ${theme.fontSize.base};
  color: ${theme.colors.greyDark};
  font-weight: 400;
  margin: ${(props) => props.margin || "12px 0"};
`;
