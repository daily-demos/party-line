import styled from "styled-components";
import theme from "../theme";

const Menu = ({ options, setIsVisible }) => {
  return (
    <Container>
      {(options || []).map((o, i) => (
        <Option
          key={i}
          warning={o.warning}
          onClick={() => {
            o.action();
            setIsVisible(false);
          }}
        >
          {o.text}
        </Option>
      ))}
    </Container>
  );
};

const Container = styled.ul`
  display: flex;
  flex-direction: column;
  background-color: ${theme.colors.white};
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.04), 0px 0px 4px rgba(0, 0, 0, 0.08);
  margin: 0;
  padding: 8px 0;
`;
const Option = styled.li`
  list-style: none;
  font-size: ${theme.fontSize.base};
  color: ${(props) =>
    props.warning ? theme.colors.redDark : theme.colors.blueDark};
  background-color: ${theme.colors.white};
  line-height: 16px;
  padding: 8px;

  &:hover {
    background-color: ${theme.colors.greyLight};
    cursor: pointer;
  }
`;

export default Menu;
