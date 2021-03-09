import styled from "styled-components";
import more from "../icons/more.svg";
import theme from "../theme";

const MoreIcon = () => {
  return <Icon src={more} />;
};

const Icon = styled.img`
  background-color: ${theme.colors.white};
  padding: 4px;
  border-radius: 24px;
`;

export default MoreIcon;
